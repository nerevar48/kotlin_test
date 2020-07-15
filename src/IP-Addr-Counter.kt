import java.io.File
import java.nio.charset.Charset
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    var remain = ""
    val bufferSize = 1024*1024*256
    var length: Int
    var linesCounter: Long = 0
    val hashSet = hashMapOf<Int, HashSet<Int>>()

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
                val arrayToHash = arrayOf<Byte>(0, 0, 0, 0)
                do {
                    var line = bufferString.substring(currentOffset, nextIndex)
                    if (remain != "") {
                        line = remain + line
                        remain = ""
                    }
                    if (line == "") {
                        break
                    }
                    fillArrayByte(arrayToHash, line)
                    addToHash(arrayToHash, hashSet)

                    currentOffset = nextIndex + 1
                    nextIndex = bufferString.indexOf('\n', currentOffset, true)
                    linesCounter ++
                } while (nextIndex != -1)
                remain = bufferString.substring(currentOffset, bufferString.length)

                println("lines remain - ${8000000000-linesCounter}")
                println("unique ip - ${getSize(hashSet)}")
                System.gc()
            }
            // последний остаток из файла
            remain = """[^\d\.]""".toRegex().replace(remain.trim(), "")
            if (remain != "") {
                val arrayToHash = arrayOf<Byte>(0, 0, 0, 0)
                fillArrayByte(arrayToHash, remain)
                addToHash(arrayToHash, hashSet)
            }
        }

        reader.close()
    }

    println("unique ip - ${getSize(hashSet)}")
    println("${executionTime/1000} seconds of execute")
}

fun addToHash(arrayToHash: Array<Byte>, hashSet: HashMap<Int, HashSet<Int>>)
{
    val hash = 31 * (31 * (31 * (31 * 1 + arrayToHash[0].hashCode()) + arrayToHash[1].hashCode()) + arrayToHash[2].hashCode()) + arrayToHash[3].hashCode()
    var hashIndex = 0
    if (hash > 0) {
        hashIndex++
    }
    if (hash % 2 == 0) {
        hashIndex++
    }
    if (hash % 3 == 0) {
        hashIndex++
    }
    if (hashSet[hashIndex] == null) {
        hashSet[hashIndex] = hashSetOf()
    }
    hashSet[hashIndex]!!.add(hash)
}

fun fillArrayByte(arrayToTrie: Array<Byte>, line: String)
{
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
}

fun getSize(hashSet: HashMap<Int, HashSet<Int>>): Int
{
    var size = 0
    hashSet.forEach {
        size += it.value.size
    }
    return size
}