import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

suspend fun main(args: Array<String>) {
    var remain = ""
    val bufferSize = 1024*1024*16
    var length: Int
    var linesCounter: Long = 0
    val trie = Trie<String>()

    val executionTime = measureTimeMillis {
        // lines 8 000 000 000
        val reader = File("./ip_addresses").inputStream()
        reader.use {
            input ->
            while (true) {
                // считывание файла кусками размерами по bufferSize
                val buffer = ByteArray(bufferSize)
                length = input.read(buffer)
                if (length <= 0)
                    break

                val bufferString = buffer.toString(Charset.defaultCharset())

                // деление на строки
                var currentOffset = 0
                var nextIndex = bufferString.indexOf('\n', currentOffset, true)
                val arrayToTrie = arrayOf<Byte>(0, 0, 0, 0)
                do {
                    var line = bufferString.substring(currentOffset, nextIndex)
                    if (remain != "") {
                        line = remain + line
                        remain = ""
                    }
                    var currentOffset1 = 0
                    var nextIndex1 = line.indexOf('.', currentOffset1, true)
                    var index = 0
                    do {
                        val str = line.substring(currentOffset1, nextIndex1)
                        val int = str.toInt()
                        val byte = int.toByte()

                        arrayToTrie[index] = byte
                        index++

                        currentOffset1 = nextIndex1 + 1
                        nextIndex1 = line.indexOf('.', currentOffset1, true)

                    } while (nextIndex1 != -1)
                    val str = line.substring(currentOffset1, line.length)
                    val int = str.toInt()
                    val byte = int.toByte()
                    arrayToTrie[index] = byte
                    trie.add(arrayToTrie)

                    currentOffset = nextIndex + 1
                    nextIndex = bufferString.indexOf('\n', currentOffset, true)
                    linesCounter ++
                } while (nextIndex != -1)

                remain = bufferString.substring(currentOffset, bufferString.length)

                println("lines remain - ${8000000000-linesCounter}")
//                println("unique ip - ${trie.size()}")
//                System.gc()
            }
            // последний остаток из файла
//            remain = """[^\d\.]""".toRegex().replace(remain.trim(), "")
//            if (remain != "") {
//                trie.add(remain, true)
//            }
        }

        reader.close()
    }

    println("unique ip - ${trie.size()}")
    println("${executionTime/1000} seconds of execute")
}

// объединено в 1 фйайл для удобства чтения

data class Node<T>(val ch: HashMap<Byte, Node<T>?> = hashMapOf())

class Trie<T>(private val root: Node<T> =  Node()) {

    fun add(values: Array<Byte>, clean: Boolean = false) {
        val array = arrayOf(values[0], values[1], values[2], values[3])
        val childrenLinks = arrayOf<HashMap<Byte, Node<T>?>?>(root.ch, null, null, null)
        /**
         *  формируем дерево байтов, например:
         *               90
         *              /   \
         *            85    86
         *            /     /  \
         *           0    -39  -40
         *          /      /\    \
         *         1     83 77   56
         */
        array.forEachIndexed { i, value ->
            if (childrenLinks[i]!![value] == null) {
                if (i == 3) {
                    childrenLinks[i]!![value] = null
                } else {
                    childrenLinks[i]!![value] = Node()
                    childrenLinks[i+1] = childrenLinks[i]!![value]!!.ch
                }
            } else {
                childrenLinks[i+1] = childrenLinks[i]!![value]!!.ch
            }
        }
    }

    fun size(): Long
    {
        var size: Long = 0

        root.ch.forEach { second ->
            second.value?.ch!!.forEach { third ->
                third.value?.ch!!.forEach { four ->
                    size += four.value?.ch!!.size
                }
            }
        }
        return size
    }

}