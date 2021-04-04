<?php
final class Base62 {
	private static $toBase62 = [
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
	] ;

	static function encode(string $str) : string {
		$bin = unpack('C*', $str) ;

		$ret = "" ;
		$value = 0 ;
		$len = floor(count($bin) / 3) * 3 ;
		$tmp = range(0, 3) ;
		$FX_bit = 0 ;
		
		$i = 0 ;
		for($i = 1; $i <= $len; $i += 3) {
			$FX_bit = 0 ;
			$value = 0 ;
			
			for($j = 0; $j < 3; ++$j) {
				if(($bin[$i+$j] & 0xF0) == 0xF0) {
					$FX_bit = 1 ;
					break ;
				}
			}

			if ($FX_bit != 0) {
				$ret .= 'z' ;

				$FX_bit = 0 ;
				for($j = 0; $j < 3; ++$j) {
					$FX_bit <<= 1 ;
					if(($bin[$i+$j] & 0xF0) == 0xF0) {
						$FX_bit |= 0x01 ;
					}
					else {
						$value <<= 4 ;
						$value |= ($bin[$i+$j] >> 4) & 0x0F ;
					}
					$value <<= 4 ;
					$value |= $bin[$i+$j] & 0x0F ;
				}
				$value |= $FX_bit << 20 ;
			}
			else {
				for($j = 0; $j < 3; ++$j)
					$value = $value * 0xF0 + ($bin[$i+$j] & 0xFF) ;
			}

			for($j = 3; $j >= 0; --$j, $value /= 61) {
				$tmp[$j] = self::$toBase62[$value % 61] ;
			}

			$ret .= join($tmp) ;
		}
		
		$len = count($bin) % 3 ;
		if($len > 0) {
			$FX_bit = 0 ;
			$value = 0 ;
			for($j = 0; $j < $len; ++$j) {
				if(($bin[$i+$j] & 0xF0) == 0xF0) {
					$FX_bit = 1 ;
					break ;
				}
			}

			if ($FX_bit != 0) {
				$ret .= 'z' ;

				$FX_bit = 0 ;
				for($j = 0; $j < $len; ++$j) {
					$FX_bit <<= 1 ;

					if(($bin[$i+$j] & 0xF0) == 0xF0) {
						$FX_bit |= 0x01 ;
					}
					else {
						$value <<= 4 ;
						$value |= ($bin[$i+$j] >> 4) & 0x0F ;
					}
					$value <<= 4 ;
					$value |= $bin[$i+$j] & 0x0F ;
				}

				$value |= $FX_bit << (($len == 1) ? 4 : 12) ;
			}
			else {
				for($j = 0; $j < $len; ++$j)
					$value = $value * 0xF0 + ($bin[$i+$j] & 0xFF) ;
			}

			$tmp = array() ;
			for($j = $len; $j >= 0; --$j, $value /= 61)
				array_unshift($tmp, self::$toBase62[$value % 61]) ;

			$ret .= join($tmp) ;
		}

		return $ret ;
	}

	private static function GetFromBase62() {
		static $fromBase62 = array() ;
		if(count($fromBase62) == 0) {
			$fromBase62 = array_fill(0, 256, -1) ;
			foreach(self::$toBase62 as $k => $v)
				$fromBase62[ord($v)] = $k ;
			$fromBase62[ord('z')] = -2 ;
		}
		return $fromBase62 ;
	}

	static function decode(string $text) : string {
		$utf8 = unpack('C*', $text) ;
		$len = count($utf8) ;
		$dst = array() ;
		$tmp = range(0, 2) ;
		$fromBase62 = self::GetFromBase62() ;
		$value = 0 ;
		$val = 0 ;
		$count = 0 ;
		$isFX = false ;

		for($i = 1; $i <= $len; ++$i) {
			$val = $fromBase62[$utf8[$i]] ;
			if($val < 0) {
				if($val == -2 && ($count % 4) == 0 && $isFX == false) {
					$isFX = true ;
					continue ;
				}

				throw new Exception("Illegal base62 character ".$utf8[$i]) ;
			}
			++$count ;

			$value = $value * 61 + $val ;
			if($count % 4 == 0) {
				if ($isFX) {
					$isFX = $value >> 20 ;

					for ($j = 2, $mask = 1; $j >= 0; --$j, $mask <<= 1) {
						$tmp[$j] = $value & 0x0F ;
						$value >>= 4 ;
					
						if (($isFX & $mask) == $mask) {
							$tmp[$j] |= 0xF0 ;
						}
						else {
							$tmp[$j] |= ($value & 0x0F) << 4 ;
							$value >>= 4 ;
						}
					}
					$isFX = false ;
				}
				else {
					for($j = 2; $j >= 0; --$j, $value /= 0xF0)
						$tmp[$j] = $value % 0xF0 ;
				}

				$value = 0 ;
				$dst = array_merge($dst, $tmp) ;
			}
		}

		$len = $count % 4 ;
		if($len > 0) {
			$len -= 1 ;
			$tmp = array() ;
			if ($isFX) {
				$isFX = $value >> (($len >= 2) ? 12 : 4) ;
				for ($j = $len-1, $mask = 1; $j >= 0; --$j, $mask <<= 1) {
					array_unshift($tmp, $value & 0x0F) ;
					$value >>= 4 ;
					if (($isFX & $mask) == $mask) {
						$tmp[$j] |= 0xF0 ;
					}
					else {
						$tmp[$j] |= ($value & 0x0F) << 4 ;
						$value >>= 4 ;
					}
				}
			}
			else {
				for($j = $len-1; $j >= 0; --$j, $value /= 0xF0)
					array_unshift($tmp, $value % 0xF0) ;
			}

			$dst = array_merge($dst, $tmp) ;
		}
		
		$ret = "" ;
		foreach($dst as $d) 
			$ret .= chr($d) ;
		return $ret ;
	}
}
?>