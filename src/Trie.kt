class Trie<T>(val root: Node<T> =  Node<T>()) {

    fun add(values: String, clean: Boolean = false) {
        val vars: ByteArray
        if (!clean) {
            vars = values.split('.').map {
                it.toShort().toByte()
            }.toByteArray()
        } else {
            vars = values.split('.').map {"""[^\d]""".toRegex().replace(it.trim(), "").toShort().toByte() }.toByteArray()
        }

        var children = root.ch
        vars.forEachIndexed { i, value ->
            if (children[value] == null) {
                if (i == vars.size-1) {
                    children[value] = null
                } else {
                    val node: Node<T> = Node<T>()
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