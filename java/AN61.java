import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * AN61(AlphaNumeric61)이란.. Text에서 특수문자를 제거한 숫자(10)+영문대문자(26)+영문소문자(25) = 61가지 문자로 변환하기<br/>
 * String To String Encoding/Decoding<br/>
 * <br/>       
 * 원리 : Text를 UTF8처리한다.<br/>
 *        여기서, Unicode값 : 0 ~ 0xFFFF(65,536가지)까지의 거의 모든 주요 나라 언어 사용한다.<br/>
 *        UTF8은 아래와 같은 Byte범위를 갖는다.
 *        그럼, 0 ~ 0x7F(127)                 --> 0xxx xxxx                     --> 0x00 ~ 0x7F<br/>
 *              0x80(128) ~ 0x7FF(2,047)      --> 110x xxxx 10xx xxxx           --> 0xC0 ~ 0xDF, 0x80 ~ 0xBF<br/>
 *              0x800(2,048) ~ 0xFFFF(65,535) --> 1110 xxxx 10xx xxxx 10xx xxxx --> 0xE0 ~ 0xEF, 0x80 ~ 0xBF, 0x80 ~ 0xBF<br/>
 *              그래서, 0x00 ~ 0x7F, 0x80 ~ 0xBF, 0xC0 ~ 0xDF, 0xE0 ~ 0xEF = 0x00 ~ 0xEF (즉, 240가지)<br/>
 *              240의 1승(240^1 = 240), 240의 2승(240^2 = 60,025), 240의 3승(240^3 = 13,824,000)<br/>
 *               61의 1승( 61^1 =  61),  61의 2승( 61^2 =  3,721),  61의 3승( 61^3 =    226,981), 61의 4승(61^4 = 13,845,841)<br/>
 *              즉, 240^3 < 61^4이다. [ BASE64처럼 256^3 = 64^4 구현하면 된다. ]<br/>
 *        <br/>      
 *        여기서, 'z'를 Padding으로 사용할 수도 있었으나, 아래와 같이 활용함.<br/>
 *        0xF0 ~ 0xFF을 escape하는 용도로 하여 Binary를 Text화 일반적인 encoding하는데 사용하였다.<br/>
 *        자세한 것은 <a href='https://github.com/zhansgsob/Base62'>https://github.com/zhansgsob/Base62</a> 참조<br/>  
 *        
 * @author zhangsob@gmail.com
 * 
 * @history 2020-08-26 encode(), decode() 만듦.<br/>
 */
public class AN61 {
	private static final char[] toBase61 = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
	};

	public static String encode(String text) throws UnsupportedEncodingException {
		byte[] utf8 = text.getBytes("utf-8");
		StringBuilder ret = new StringBuilder() ;
		int value = 0 ;
		int val = 0 ;
		int len = utf8.length ;
		char[] tmp = new char[4] ;
		for(int i = 0; i < len; ++i) {
			val = (utf8[i] & 0xFF) ;
			if(val >= 0xF0)	{
				for(int j = 0; j < text.length(); ++j)
					if(text.codePointAt(j) > 0xFFFF) {
						throw new UnsupportedEncodingException("invalid UCS2 character index " + j + " " + text.substring(j, j+2)) ;
					}
			}

			value = value * 0xF0 + val;
			if(i % 3 == 2) {
				for(int j = 3; j >= 0; --j, value /= 61)
					tmp[j] = toBase61[value % 61];

				value = 0 ;
				ret.append(tmp, 0, 4) ;
			}
		}
		
		len = utf8.length % 3 ;
		if(len > 0) {
			for(int j = len; j >= 0; --j, value /= 61)
				tmp[j] = toBase61[value % 61];

			ret.append(tmp, 0, len+1) ;
		}

		return ret.toString() ;
	}

	private static final int[] fromBase61 = new int[128] ;
	static {
		Arrays.fill(fromBase61, -1);
		for (int i = 0, len = toBase61.length; i < len; i++)
			fromBase61[toBase61[i]] = i;
	}

	public static String decode(String text) throws UnsupportedEncodingException {
		int len = text.length() ;
		if(len % 4 == 1)	throw new IllegalArgumentException("invalid AN61 length") ;

		byte[] dst = new byte[len / 4 * 3 + ((len % 4 > 0) ? len%4 - 1 : 0)] ;
		byte[] tmp = new byte[3] ;
		int value = 0 ;
		int val = 0 ;
		char ch = 0 ;

		int bi = 0 ;
		for(int i = 0; i < len; ++i) {
			ch = text.charAt(i) ;
			if(ch >= 0x80)
				throw new IllegalArgumentException("invalid AN61 character " + ch) ;
			
			val = fromBase61[ch] ;
			if(val < 0)
				throw new IllegalArgumentException("invalid AN61 character " + ch) ;
			
			value = value * 61 + val;
			if(i % 4 == 3) {
				for(int j = 2; j >= 0; --j, value /= 0xF0)
					tmp[j] = (byte)(value % 0xF0) ;

				value = 0 ;
				System.arraycopy(tmp, 0, dst, bi, 3);
				bi += 3 ;
			}
		}

		len = len % 4 ;
		if(len > 0) {
			len -= 1 ;
			for(int j = len-1; j >= 0; --j, value /= 0xF0)
				tmp[j] = (byte)(value % 0xF0) ;

			System.arraycopy(tmp, 0, dst, bi, len);
			bi += len ;
		}

		return new String(dst, 0, bi, "utf-8");
	}

	public static void main(String[] args) {
		try {
			String src0 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可" ;
			System.out.println("src0["+src0.length()+"]:" + src0) ;
			String an61__tmp0 = AN61.encode(src0) ;
			System.out.println("an61__tmp0["+an61__tmp0.length()+"]:" + an61__tmp0) ;
			String an61__out0 = AN61.decode(an61__tmp0) ;
			System.out.println("an61__out0["+an61__out0.length()+"]:" + an61__out0) ;
			
			System.out.println("src0.equals(an61__out0) : " + src0.equals(an61__out0)) ;
			
			String base64_tmp = java.util.Base64.getEncoder().encodeToString(src0.getBytes("utf8")) ;
			System.out.println("base64_tmp["+base64_tmp.length()+"]:" + base64_tmp) ;
			String base64_out = new String(java.util.Base64.getDecoder().decode(base64_tmp), "utf8") ;
			System.out.println("base64_out["+base64_out.length()+"]:" + base64_out) ;

			// [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
			String src1 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可🐘" ;	// UnsupportedEncodingException이 발생하는 경우
			System.out.println("src1["+src1.length()+"]:" + src1) ;		// String.length()은 문자갯수가 아니라, UTF16의 길이다. 
			try {
				String tmp1 = AN61.encode(src1) ;
				System.out.println("tmp1:" + tmp1) ;
				String out1 = AN61.decode(tmp1) ;
				System.out.println("out1:" + out1) ;
			} catch(UnsupportedEncodingException uee) {
				System.err.println(uee) ;

				String tmp2 = Base62.encode(src1.getBytes("utf8")) ;
				System.out.println("tmp2["+tmp2.length()+"]:" + tmp2) ;
				String out2 = new String(Base62.decode(tmp2), "utf8") ;
				System.out.println("out2["+out2.length()+"]:" + out2) ;
				
				System.out.println("src1.equals(out2) : " + src1.equals(out2)) ;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
