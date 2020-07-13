import java.io.File
import java.nio.charset.Charset
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    var remain = ""
    var list: List<String>
    val bufferSize = 1024*1024
    var buffer: ByteArray
    var length: Int
    var last: Int
    var line: String
    var linesCounter: Long = 0
    val trie = Trie<Byte>()
    var start: Long
    var stop: Long

    val executionTime = measureTimeMillis {
        // lines 8 000 000 000
        val reader = File("./ip_addresses").inputStream().buffered()
        reader.use {
            input ->
            while (true) {
                start = System.currentTimeMillis()

                buffer = ByteArray(bufferSize)
                length = input.read(buffer)
                if (length <= 0)
                    break

                list = buffer.toString(Charset.defaultCharset()).split('\n')
                linesCounter += list.size
                last = list.lastIndex
                for (i in 0 until last) {
                    line = list[i]
                    if (remain != "") {
                        line = remain + line
                        remain = ""
                    }
                    if (line == "") {
                        continue
                    }

                    trie.add(line)
                }
                remain = list[last]

                stop = System.currentTimeMillis()

                println("in process lines - $linesCounter")
                println("lines remain - ${8000000000-linesCounter}")
                println("line process remain - ${((stop - start).toDouble() / list.size) * (8000000000-linesCounter) / 1000}")
                println("1kk line process time - ${(stop - start).toDouble()/1000/list.size.toDouble()*1000000}")
                println("unique ip - ${trie.size()}")
            }
            remain = """[^\d\n\.]""".toRegex().replace(remain.trim(), "")
            if (remain != "") {
                trie.add(remain, true)
            }
        }

        reader.close()
    }

    println("unique ip - ${trie.size()}")
    println("${executionTime/1000} seconds of execute")
}