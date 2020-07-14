import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

suspend fun main(args: Array<String>) {
    var remain = ""
    val bufferSize = 1024*1024*8
    var length: Int
    var linesCounter: Long = 0
    val trie = Trie<Byte>()

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
                val list = bufferString.split("\n")

                linesCounter += list.size
                /**
                 *  запоминание последнего элемента, т.к он обрезан при доставании из файла
                 */
                val last = list.lastIndex

                for (i in 0 until last) {
                    if (list[i] == "" && remain == "") {
                        continue
                    }
                    var line = list[i]
                    // приклеиваем остаток из прошлой итерации к первой строке в новой итерации
                    if (remain != "") {
                        line = remain + line
                        remain = ""
                    }
                    // добавляем в дерево
                    trie.add(line)
                }

                remain = list[last]

                println("lines remain - ${8000000000-linesCounter}")
                println("unique ip - ${trie.size()}")

                System.gc()
            }
            // последний остаток из файла
            remain = """[^\d\.]""".toRegex().replace(remain.trim(), "")
            if (remain != "") {
                trie.add(remain, true)
            }
        }

        reader.close()
    }

    println("unique ip - ${trie.size()}")
    println("${executionTime/1000} seconds of execute")
}

data class Node<T>(val ch: HashMap<Byte, Node<T>?> = hashMapOf())

class Trie<T>(val root: Node<T> =  Node<T>()) {

    fun add(values: String, clean: Boolean = false) {
        val varsString: Sequence<String>
        val vars: Sequence<Byte>

        // превращаем строку в массив байтов
        if (!clean) {
            varsString = values.split('.').asSequence()
            vars = varsString.map {
                it.toShort().toByte()
            }
        } else {
            varsString = values.split('.').asSequence()
            vars = varsString.map {"""[^\d]""".toRegex().replace(it.trim(), "").toShort().toByte() }
        }

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
        var children = root.ch
        vars.forEachIndexed { i, value ->
            if (children[value] == null) {
                if (i == 3) {
                    children[value] = null
                } else {
                    val node: Node<T> = Node()
                    children[value] = node
                    children = node.ch
                }
            } else {
                val node: Node<T> = children[value]!!
                children = node.ch
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