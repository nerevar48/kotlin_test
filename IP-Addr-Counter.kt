import java.io.File
import java.nio.charset.Charset

fun main(args: Array<String>) {
    val hashSet = mutableSetOf<String>()
    var remain = ""
    var list: List<String>

    val reader = File("./ipList.txt").inputStream()
    reader.use {
        input ->
        while (true) {
            val buffer = ByteArray(8192)
            val length = input.read(buffer)
            if (length <= 0)
                break

            list = buffer.toString(Charset.defaultCharset()).split('\n')
            val last = list.lastIndex
            for (i in 0 until last) {
                var line = list[i]
                if (remain != "") {
                    line = remain + line
                    remain = ""
                }
                val chars = line.split('.').map {
                    (it.toShort() + 1).toChar()
                }.joinToString("")
                hashSet.add(chars)
            }
            remain = list[last]
        }
        if (remain != "") {
            val chars = remain.split('.').map {
                val regex = """[^\d]""".toRegex()
                (regex.replace(it, "").toShort() + 1).toChar()
            }.joinToString("")
            hashSet.add(chars)
        }
    }
    reader.close()
    println(hashSet.size)
}