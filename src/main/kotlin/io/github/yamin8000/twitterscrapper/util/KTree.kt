package io.github.yamin8000.twitterscrapper.util

class KTree<T>(root : T) {
    var parent: KTree<T>? = null
        get() = field
    var data: T = root
        get() = field

    val isRoot: Boolean
        get() = parent == null

    val isLeaf: Boolean
        get() = directChildren.isEmpty()

    val level: Int
        get() = if (isRoot) 0 else (parent?.level ?: 0) + 1

    private var directChildren = mutableListOf<KTree<T>>()

    private var descendants = mutableListOf<KTree<T>>()

    fun children() = directChildren.toList()

    fun root(): KTree<T> = if (this.parent == null) this else this.root()

    fun addChild(child: T): KTree<T> {
        val childNode = KTree(child)
        childNode.parent = this
        directChildren.add(childNode)
        addDescendant(childNode)
        parent?.addDescendant(childNode)
        return childNode
    }

    private fun addDescendant(descendant: KTree<T>) {
        descendants.add(descendant)
    }

    fun findChild(node: T) = directChildren.find { it.data == node }

    fun findDescendant(node: T) = findChild(node) ?: descendants.find { it.data == node }

    override fun toString() = data.toString()
}