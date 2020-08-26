import java.util.Arrays;

/**
 * BASE62.. Text에서 특수문자를 제거한 숫자(10)+영문대문자(26)+영문소문자(26) = 62가지 문자로 변환하기<br/>
 * <br/>       
 * 원리 : 0x00 ~ 0xEF까지는 0~9,A~Z,a~y로 AN62(<a href='https://github.com/zhansgsob/AN62'>https://github.com/zhansgsob/AN62</a> 참조)로 표현이 가능하다.<br/>               
 *        'z'를 0xF0 ~ 0xFF을 escape하는 용도로 한다.<br/>
 *		  <br/>        
 *        0xXx 0xYy 0xFz : 0x1XxYyz<br/>
 *        0xXx 0xFy 0xZz : 0x2XxyZz<br/>
 *        0xXx 0xFy 0xFz : 0x30Xxyz<br/>
 *        0xFx 0xYy 0xZz : 0x4xYyZz<br/>
 *        0xFx 0xYy 0xFz : 0x50xYyz<br/>
 *        0xFx 0xFy 0xZz : 0x60xyZz<br/>
 *        0xFx 0xFy 0xFz : 0x700xyz<br/>
 *        -------------------------<br/>
 *        즉, 23bit로 표현가능하다. ( 2^23 < 61^4 )<br/>
 *        하위 20bit를 값을 AN62처리하고, 상위 3bit로 0xFX의 위치를 표시한다.<br/> 
 *        
 * @author zhangsob@gmail.com
 * 
 * @history 2020-08-24 encode(), decode() 만듦.<br/>
 */
public class Base62 {
	private static final char[] toBase62 = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
	};

	private static final int[] fromBase62 = new int[128] ;
	static {
		Arrays.fill(fromBase62, -1);
		for (int i = 0, len = toBase62.length; i < len; i++)
			fromBase62[toBase62[i]] = i;
		fromBase62['z'] = -2;
	}

	public static String encode(byte[] bin)
	{
		StringBuilder ret = new StringBuilder() ;
		int value = 0 ;
		int len = bin.length / 3 * 3 ;
		char[] tmp = new char[4] ;
		int FX_bit = 0 ;
		int i = 0 ;

		for(i = 0; i < len; i += 3) {
			FX_bit = 0 ;
			value = 0 ;
			
			for(int j = 0; j < 3; ++j) {
				if((bin[i+j] & 0xF0) == 0xF0) {
					FX_bit = 1 ;
					break ;
				}
			}

			if (FX_bit != 0) {
				ret.append('z') ;

				FX_bit = 0 ;	
				for(int j = 0; j < 3; ++j) {
					FX_bit <<= 1 ;
					if((bin[i+j] & 0xF0) == 0xF0) {
						FX_bit |= 0x01 ;
					}
					else {
						value <<= 4 ;
						value |= (bin[i+j] >> 4) & 0x0F ;
					}
					value <<= 4 ;
					value |= bin[i+j] & 0x0F ;
				}
				value |= FX_bit << 20 ;
			}
			else {
				for(int j = 0; j < 3; ++j)
					value = value * 0xF0 + (bin[i+j] & 0xFF) ;
			}

			for(int j = 3; j >= 0; --j, value /= 61)
				tmp[j] = toBase62[value % 61] ;

			ret.append(tmp, 0, 4) ;
		}

		len = bin.length % 3 ;
		if(len > 0) {
			FX_bit = 0 ;
			value = 0 ;
			for(int j = 0; j < len; ++j) {
				if((bin[i+j] & 0xF0) == 0xF0) {
					FX_bit = 1 ;
					break ;
				}
			}

			if (FX_bit != 0) {
				ret.append('z') ;

				FX_bit = 0 ;
				for(int j = 0; j < len; ++j) {
					FX_bit <<= 1 ;
					
					if((bin[i+j] & 0xF0) == 0xF0) {
						FX_bit |= 0x01 ;
					}
					else {
						value <<= 4 ;
						value |= (bin[i+j] >> 4) & 0x0F ;
					}
					value <<= 4 ;
					value |= bin[i+j] & 0x0F ;
				}

				value |= FX_bit << ((len == 1) ? 4 : 12) ;
			}
			else {
				for(int j = 0; j < len; ++j)
					value = value * 0xF0 + (bin[i+j] & 0xFF) ;
			}

			for(int j = len; j >= 0; --j, value /= 61)
				tmp[j] = toBase62[value % 61] ;

			ret.append(tmp, 0, len+1) ;
		}

		return ret.toString() ;
	}
	
	public static byte[] decode(String txt)
	{
		int len = txt.length() ;

		byte[] dst = new byte[len / 4 * 3 + ((len % 4 > 0) ? len%4 - 1 : 0)] ;
		byte[] tmp = new byte[3] ;

		int value = 0 ;
		int val = 0 ;
		char ch = 0 ;
		int count = 0 ;	
		int bi = 0 ;
		int isFX = 0 ;
		for(int i = 0; i < len; ++i) {
			ch = txt.charAt(i) ;
			if(ch >= 0x80)
				throw new IllegalArgumentException("Illegal base62 character " + ch) ;

			val = fromBase62[ch] ;
			if(val < 0) {
				if(val == -2 && (count % 4) == 0 && isFX == 0) {
					isFX = 1 ;
					continue ;
				}

				throw new IllegalArgumentException("Illegal base62 character " + ch) ;
			}
			++count ;

			value = value * 61 + val ;
			if(count % 4 == 0) {
				if (isFX != 0) {
					isFX = value >> 20 ;
					for (int j = 2, mask = 1; j >= 0; --j, mask <<= 1) {
						tmp[j] = (byte)(value & 0x0F) ;	value >>= 4 ;
						if ((isFX & mask) == mask)	{	tmp[j] |= 0xF0 ;	}
						else						{	tmp[j] |= (value & 0x0F) << 4 ;	value >>= 4 ;	}
					}
					isFX = 0 ;
				}
				else {
					for(int j = 2; j >= 0; --j, value /= 0xF0)
						tmp[j] = (byte)(value % 0xF0) ;
				}

				value = 0 ;
				System.arraycopy(tmp, 0, dst, bi, 3) ;
				bi += 3 ;
			}
		}

		len = count % 4 ;
		if(len > 0) {
			len -= 1 ;
			if (isFX != 0) {
				isFX = value >> ((len >= 2) ? 12 : 4) ;
				for (int j = len-1, mask = 1; j >= 0; --j, mask <<= 1) {
					tmp[j] = (byte)(value & 0x0F) ;	value >>= 4 ;
					if ((isFX & mask) == mask)	{	tmp[j] |= 0xF0 ;	}
					else						{	tmp[j] |= (value & 0x0F) << 4 ;	value >>= 4 ;	}
				}
			}
			else {
				for(int j = len-1; j >= 0; --j, value /= 0xF0)
					tmp[j] = (byte)(value % 0xF0) ;
			}

			System.arraycopy(tmp, 0, dst, bi, len) ;
			bi += len ;
		}

		if(bi < dst.length) {
			byte[] ret = new byte[bi] ;
			System.arraycopy(dst, 0, ret, 0, bi) ;
			return ret ;
		}

		return dst ;
	}
	
	private static String toHexa(byte[] bin) {
		StringBuilder ret = new StringBuilder() ;

		for(int i = 0, len = bin.length; i < len; ++i) {
			ret.append(String.format("%02X ", bin[i])) ;
			switch(i % 16) {
			case 7 : ret.append(" ") ;	break ;
			case 15: ret.append("\n") ;	break ;
			}
		}

		if (bin.length % 16 != 0)
			ret.append("\n") ;

		return ret.toString() ;
	}

	public static void main(String[] args) {
		try {
			byte[] bin = new byte[256] ;
			for(int i = 0; i <= 0xFF; ++i)
				bin[i] = (byte)i ;
				
			System.out.println("----bin["+bin.length+"]----") ;
			System.out.println(toHexa(bin)) ;

			String txt = Base62.encode(bin) ;
			System.out.println("txt:" + txt) ;

			byte[] out = Base62.decode(txt) ;
			System.out.println("----out["+out.length+"]----") ;
			System.out.println(toHexa(out)) ;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
