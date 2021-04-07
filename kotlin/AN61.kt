
import java.io.UnsupportedEncodingException
import java.util.*


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
 * @history 2021-04-07 encode(), decode() 만듦.<br/>
 */
class AN61 {
    companion object {
        @JvmStatic
        private val toBase61 = charArrayOf(
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y')

        @JvmStatic
        @Throws(UnsupportedEncodingException::class)
        fun encode(text: String): String {
            val utf8 = text.toByteArray(charset("utf-8"))
            val ret = StringBuilder()
            var value = 0
            val tmp = CharArray(4)
            for (i in utf8.indices) {
                val v = utf8[i].toInt() and 0xFF
                if (v >= 0xF0) {
                    for (j in text.indices) {
                        if (text.codePointAt(j) > 0xFFFF)
                            throw UnsupportedEncodingException("invalid UCS2 character index " + j + " " + text.substring(j, j + 2))
                    }
                }

                value = value * 0xF0 + v

                if (i % 3 == 2) {
                    for(j in 3 downTo 0)  {
                        tmp[j] = toBase61[value % 61]
                        value /= 61
                    }
                    value = 0

                    ret.append(tmp, 0, 4)
                }
            }

            val len = utf8.size % 3
            if (len > 0) {
                for(j in len downTo 0) {
                    tmp[j] = toBase61[value % 61]
                    value /= 61
                }

                ret.append(tmp, 0, len + 1)
            }

            return ret.toString()
        }

        @JvmStatic
        @Throws(UnsupportedEncodingException::class)
        fun decode(text: String): String {
            var len = text.length
            require(len % 4 != 1) { "invalid AN61 length" }

            val dst = ByteArray(len / 4 * 3 + if (len % 4 > 0) len % 4 - 1 else 0)
            val tmp = ByteArray(3)

            val fromBase61 = IntArray(128)
            Arrays.fill(fromBase61, -1)
            for (i in toBase61.indices)
                fromBase61[toBase61[i].toInt()] = i

            var value = 0
            var bi = 0
            for (i in text.indices) {
                val ch = text[i]
                require(ch.toInt() < 0x80) { "invalid AN61 character $ch" }
                val v = fromBase61[ch.toInt()]
                require(v >= 0) { "invalid AN61 character $ch" }
                value = value * 61 + v
                if (i % 4 == 3) {
                    for(j in 2 downTo 0) {
                        tmp[j] = (value % 0xF0).toByte()
                        value /= 0xF0
                    }

                    value = 0
                    System.arraycopy(tmp, 0, dst, bi, 3)
                    bi += 3
                }
            }

            len %= 4
            if (len > 0) {
                len -= 1
                for(j in len-1 downTo 0) {
                    tmp[j] = (value % 0xF0).toByte()
                    value /= 0xF0
                }

                System.arraycopy(tmp, 0, dst, bi, len)
                bi += len
            }

            return String(dst, 0, bi, charset("utf-8"))
        }
    }
}