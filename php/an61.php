<?php
final class AN61 {
	private static $toBase61 = [
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
	] ;

	static function encode(string $text) : string {
		$utf8 = unpack('C*', $text) ;
		$ret = "" ;
		$value = 0 ;
		$tmp = range(0, 3) ;

		$i = 0 ;
		foreach($utf8 as $val) {
			if($val >= 0xF0)
				throw new Exception("invalid UCS2 character") ;

			$value = $value * 0xF0 + $val;
			if(++$i % 3 == 0) {
				for($j = 3; $j >= 0; --$j, $value /= 61)
					$tmp[$j] = self::$toBase61[$value % 61] ;

				$value = 0 ;
				$ret .= join('', $tmp) ;
			}
		}

		$len = count($utf8) % 3 ;
		if($len > 0) {
			$tmp = array() ;
			for($j = $len; $j >= 0; --$j, $value /= 61)
				array_unshift($tmp, self::$toBase61[$value % 61]) ;

			$ret .= join('', $tmp) ;
		}

		return $ret ;
	}

	private static function GetFromBase61() {
		static $fromBase61 = array() ;
		if(count($fromBase61) == 0) {
			$fromBase61 = array_fill(0, 256, -1) ;
			foreach(self::$toBase61 as $k => $v)
				$fromBase61[ord($v)] = $k ;
		}
		return $fromBase61 ;
	}

	static function decode(string $text) : string {
		$utf8 = unpack('C*', $text) ;
		$len = count($utf8) ;
		if($len % 4 == 1)	throw new Exception("invalid AN61 length") ;
		$fromBase61 = self::GetFromBase61() ;
		$dst = "" ;
		$value = 0 ;
		$tmp = range(0, 2) ;
		
		$i = 0 ;
		foreach($utf8 as $b) {
			$val = $fromBase61[$b] ;
			if($val < 0)
				throw new Exception("invalid AN61 character ".chr($val)) ;
			
			$value = $value * 61 + $val;
			if(++$i % 4 == 0) {
				for($j = 2; $j >= 0; --$j, $value /= 0xF0)
					$tmp[$j] = chr($value % 0xF0) ;

				$value = 0 ;
				$dst .= join('', $tmp) ;
			}
		}

		$len = $len % 4 ;
		if($len > 0) {
			$tmp = array() ;
			$len -= 1 ;
			for($j = $len-1; $j >= 0; --$j, $value /= 0xF0)
				array_unshift($tmp, chr($value % 0xF0)) ;

			$dst .= join('', $tmp) ;
		}

		return $dst ;
	}
}
?>