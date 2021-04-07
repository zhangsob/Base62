private fun bin2hexa(bin: ByteArray): String {
    val ret = StringBuilder()
    for (i in bin.indices) {
        ret.append(String.format("%02X ", bin[i]))
        when (i % 16) {
            7 -> ret.append(" ")
            15 -> ret.append("\n")
        }
    }
    if (bin.size % 16 != 0) ret.append("\n")
    return ret.toString()
}

fun main(args: Array<String>) {
    try {
        val bin = ByteArray(256)
        for (i in bin.indices) bin[i] = i.toByte()
        println("----bin[" + bin.size + "]----")
        println(bin2hexa(bin))
        val txt = Base62.encode(bin)
        println("txt[" + txt.length + "]:" + txt)
        val out = Base62.decode(txt)
        println("----out[" + out.size + "]----")
        println(bin2hexa(out))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}