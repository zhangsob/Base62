
import java.util.*


/**
 * BASE62.. Text에서 특수문자를 제거한 숫자(10)+영문대문자(26)+영문소문자(26) = 62가지 문자로 변환하기<br/>
 * <br/>
 * 원리 : 0x00 ~ 0xEF까지는 0~9,A~Z,a~y로 AN61(<a href='https://github.com/zhansgsob/Base62'>https://github.com/zhansgsob/Base62</a> 참조)로 표현이 가능하다.<br/>
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
 * @history 2021-04-07 encode(), decode() 만듦.<br/>
 */
class Base62 {
    companion object {
        @JvmStatic
        private val toBase62 = charArrayOf(
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')

        @JvmStatic
        fun encode(bin: ByteArray): String {
            val ret = StringBuilder()
            var value = 0
            var len = bin.size / 3 * 3
            val tmp = CharArray(4)
            var FX_bit = 0
            for(i in 0 until len step 3) {
                FX_bit = 0
                value = 0

                for (j in 0..2) {
                    if (bin[i + j].toInt() and 0xF0 == 0xF0) {
                        FX_bit = 1
                        break
                    }
                }

                if (FX_bit != 0) {
                    ret.append('z')

                    FX_bit = 0
                    for (j in 0..2) {
                        FX_bit = FX_bit shl 1
                        if (bin[i + j].toInt() and 0xF0 == 0xF0) {
                            FX_bit = FX_bit or 0x01
                        } else {
                            value = value shl 4
                            value = value or (bin[i + j].toInt() shr 4 and 0x0F)
                        }
                        value = value shl 4
                        value = value or (bin[i + j].toInt() and 0x0F)
                    }

                    value = value or (FX_bit shl 20)
                } else {
                    for (j in 0..2)
                        value = value * 0xF0 + (bin[i + j].toInt() and 0xFF)
                }

                for(j in 3 downTo 0) {
                    tmp[j] = toBase62[value % 61]
                    value /= 61
                }

                ret.append(tmp, 0, 4)
            }

            val i = len
            len = bin.size % 3
            if (len > 0) {
                FX_bit = 0
                value = 0
                for (j in 0 until len) {
                    if (bin[i + j].toInt() and 0xF0 == 0xF0) {
                        FX_bit = 1
                        break
                    }
                }

                if (FX_bit != 0) {
                    ret.append('z')

                    FX_bit = 0
                    for (j in 0 until len) {
                        FX_bit = FX_bit shl 1
                        if (bin[i + j].toInt() and 0xF0 == 0xF0) {
                            FX_bit = FX_bit or 0x01
                        } else {
                            value = value shl 4
                            value = value or (bin[i + j].toInt() shr 4 and 0x0F)
                        }

                        value = value shl 4
                        value = value or (bin[i + j].toInt() and 0x0F)
                    }

                    value = value or (FX_bit shl if (len == 1) 4 else 12)
                } else {
                    for (j in 0 until len)
                        value = value * 0xF0 + (bin[i + j].toInt() and 0xFF)
                }

                for(j in len downTo 0) {
                    tmp[j] = toBase62[value % 61]
                    value /= 61
                }

                ret.append(tmp, 0, len + 1)
            }

            return ret.toString()
        }

        @JvmStatic
        fun decode(txt: String): ByteArray {
            var len = txt.length
            val dst = ByteArray(len / 4 * 3 + if (len % 4 > 0) len % 4 - 1 else 0)
            val tmp = ByteArray(3)

            val fromBase62 = IntArray(128)
            Arrays.fill(fromBase62, -1)
            for (i in toBase62.indices)
                fromBase62[toBase62[i].toInt()] = i
            fromBase62['z'.toInt()] = -2

            var value = 0
            var count = 0
            var bi = 0
            var isFX = 0
            for (i in txt.indices) {
                val ch = txt[i]
                require(ch.toInt() < 0x80) { "Illegal base62 character $ch" }

                val v = fromBase62[ch.toInt()]
                if (v < 0) {
                    if (v == -2 && count % 4 == 0 && isFX == 0) {
                        isFX = 1
                        continue
                    }

                    throw IllegalArgumentException("Illegal base62 character $ch")
                }

                ++count

                value = value * 61 + v
                if (count % 4 == 0) {
                    if (isFX != 0) {
                        isFX = value shr 20

                        var mask = 1
                        for (j in 2 downTo 0) {
                            tmp[j] = (value and 0x0F).toByte()
                            value = value shr 4
                            if (isFX and mask == mask) {
                                tmp[j] = (tmp[j].toInt() or 0xF0).toByte()
                            } else {
                                tmp[j] = (tmp[j].toInt() or (value and 0x0F shl 4)).toByte()
                                value = value shr 4
                            }
                            mask = mask shl 1
                        }
                        isFX = 0

                    } else {
                        for(j in 2 downTo 0) {
                            tmp[j] = (value % 0xF0).toByte()
                            value /= 0xF0
                        }
                    }

                    value = 0
                    System.arraycopy(tmp, 0, dst, bi, 3)
                    bi += 3
                }
            }

            len = count % 4
            if (len > 0) {
                len -= 1
                if (isFX != 0) {
                    isFX = value shr if (len >= 2) 12 else 4
                    var mask = 1
                    for(j in len -1 downTo 0) {
                        tmp[j] = (value and 0x0F).toByte()
                        value = value shr 4
                        if (isFX and mask == mask) {
                            tmp[j] = (tmp[j].toInt() or 0xF0).toByte()
                        } else {
                            tmp[j] = (tmp[j].toInt() or (value and 0x0F shl 4)).toByte()
                            value = value shr 4
                        }
                        mask = mask shl 1
                    }
                } else {
                    for(j in len -1 downTo 0) {
                        tmp[j] = (value % 0xF0).toByte()
                        value /= 0xF0
                    }
                }

                System.arraycopy(tmp, 0, dst, bi, len)
                bi += len
            }

            if (bi < dst.size) {
                val ret = ByteArray(bi)
                System.arraycopy(dst, 0, ret, 0, bi)
                return ret
            }

            return dst
        }
    }
}