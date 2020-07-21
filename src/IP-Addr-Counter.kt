import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {

//    val counter = IpCounter("./ipList2")
    val counter = IpCounter("./ip_addresses")
    counter.run()

    println("unique ip - ${counter.getSize()}")
    println("${counter.executionTime/1000} seconds of execute")
}


class IpCounter(private val filePath: String)
{
    var executionTime = 0L
    var bufferSize = 1024*1024*256

    private var remain = ""
    private var length: Int = 0
    private var linesCounter: Long = 0
    private var line = ""
    private val arrayToHash = arrayOf<Short>(0, 0, 0, 0)
    private var buffer = ByteArray(bufferSize)
    private var bufferString = ""
    private val array1 = ByteArray((Int.MAX_VALUE/2)+1)
    private val array2 = ByteArray((Int.MAX_VALUE/2)+1)


    fun run()
    {
        executionTime = measureTimeMillis {
            val reader = File(filePath).inputStream()
            reader.use {
                input ->
                while (true) {
                    // считывание файла кусками размерами по bufferSize
                    buffer = ByteArray(bufferSize)
                    length = input.read(buffer)
                    if (length <= 0)
                        break

                    bufferString = buffer.toString(Charset.defaultCharset())
                    buffer = ByteArray(bufferSize)

                    // деление на строки
                    var currentOffset = 0
                    var nextIndex = bufferString.indexOf('\n', currentOffset, true)
                    do {

                        line = bufferString.substring(currentOffset, nextIndex)
                        if (remain != "") {
                            line = remain + line
                            remain = ""
                        }
                        if (line == "") {
                            break
                        }

                        fillArray()
                        addToHash()

                        currentOffset = nextIndex + 1
                        nextIndex = bufferString.indexOf('\n', currentOffset, true)
                        linesCounter ++
                    } while (nextIndex != -1)
                    remain = bufferString.substring(currentOffset, bufferString.length)

                    println("lines remain - ${8000000000-linesCounter}")

                }
                // последний остаток из файла
                remain = """[^\d\.]""".toRegex().replace(remain.trim(), "")
                if (remain != "") {
                    line = remain
                    fillArray()
                    addToHash()
                }
            }

            reader.close()
        }

    }

    /**
     * добавление в один из хеш сетов контент-хеш массива байтов
     * разбито на несколько хешей для скорости
     */
    private fun addToHash()
    {
//    val chars: String = arrayToHash[0].toChar().toString() + arrayToHash[1].toChar().toString() + arrayToHash[2].toChar().toString() + arrayToHash[3].toChar().toString()
        val hashIndex = when (arrayToHash[0]) {
            in 0..127 -> {
                0
            }
            else -> 1
        }
        val int = arrayToHash[0]*256*256*256 + arrayToHash[1]*256*256 + arrayToHash[2]*256 + arrayToHash[3]

        if (hashIndex == 0) {
            if (int > (Int.MAX_VALUE / 2)) {
                val halfInt = int - (Int.MAX_VALUE / 2) - 1

                if (array2[halfInt] == 0.toByte() || array2[halfInt] == 10.toByte()) {
                    array2[halfInt] = (array2[halfInt] + 1).toByte()
                }
            } else {
                if (array1[int] == 0.toByte() || array1[int] == 10.toByte()) {
                    array1[int] = (array1[int] + 1).toByte()
                }
            }
        } else {
            val invertInt = int + Int.MAX_VALUE + 1
            if (invertInt > (Int.MAX_VALUE / 2)) {
                val halfInt = invertInt - (Int.MAX_VALUE / 2) - 1

                if (array2[halfInt] in 0..1) {
                    array2[halfInt] = (array2[halfInt] + 10).toByte()
                }
            } else {
                if (array1[invertInt] in 0..1) {
                    array1[invertInt] = (array1[invertInt] + 10).toByte()
                }
            }
        }

//        hashSet[hashIndex]!!.add(arrayToHash[0]*1000000000L+arrayToHash[1]*1000000+arrayToHash[2]*1000+arrayToHash[3])
    }

    /**
     * превращение строки ip адреса в массив
     */
    private fun fillArray()
    {
        var currentOffset1 = 0
        var nextIndex1 = line.indexOf('.', currentOffset1, true)
        var index = 0
        do {
            val str = line.substring(currentOffset1, nextIndex1)
            arrayToHash[index] = str.toShort()
            index++

            currentOffset1 = nextIndex1 + 1
            nextIndex1 = line.indexOf('.', currentOffset1, true)
        } while (nextIndex1 != -1)
        val str = line.substring(currentOffset1, line.length)
        arrayToHash[index] = str.toShort()
    }

    /**
     * получение количества уникальных ip
     */
    fun getSize(): Int
    {
        var size = 0
        array1.forEach {
            if (it == 1.toByte()) {
                size++
            } else if (it == 11.toByte()) {
                size += 2
            }
        }
        array2.forEach {
            if (it == 1.toByte()) {
                size++
            } else if (it == 11.toByte()) {
                size += 2
            }
        }
        return size
    }
}

