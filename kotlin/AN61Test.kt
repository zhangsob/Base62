import java.io.UnsupportedEncodingException
import java.util.*

fun main(args: Array<String>) {
    try {
        val src0 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可"
        println("src0[" + src0.length + "]:" + src0)
        val an61__tmp0: String = AN61.encode(src0)
        println("an61__tmp0[" + an61__tmp0.length + "]:" + an61__tmp0)
        val an61__out0: String = AN61.decode(an61__tmp0)
        println("an61__out0[" + an61__out0.length + "]:" + an61__out0)
        println("src0.equals(an61__out0) : " + (src0 == an61__out0))
        val base64_tmp = Base64.getEncoder().encodeToString(src0.toByteArray(charset("utf8")))
        println("base64_tmp[" + base64_tmp.length + "]:" + base64_tmp)
        val base64_out = String(Base64.getDecoder().decode(base64_tmp), charset("utf8"))
        println("base64_out[" + base64_out.length + "]:" + base64_out)

        // [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
        val src1 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可🐘" // UnsupportedEncodingException이 발생하는 경우
        println("src1[" + src1.length + "]:" + src1) // String.length()은 문자갯수가 아니라, UTF16의 길이다.
        try {
            val tmp1: String = AN61.encode(src1)
            println("tmp1:$tmp1")
            val out1: String = AN61.decode(tmp1)
            println("out1:$out1")
        } catch (uee: UnsupportedEncodingException) {
            System.err.println(uee)
            val tmp2: String = Base62.encode(src1.toByteArray(charset("utf8")))
            println("tmp2[" + tmp2.length + "]:" + tmp2)
            val out2 = String(Base62.decode(tmp2), charset("utf8"))
            println("out2[" + out2.length + "]:" + out2)
            println("src1.equals(out2) : " + (src1 == out2))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}