package com.digia.digiaui.framework.state


class StateTree {

    private val childrenMap =
        mutableMapOf<StateContext, MutableSet<StateContext>>()

    fun attach(parent: StateContext?, child: StateContext) {
        if (parent == null) return
        childrenMap.getOrPut(parent) { mutableSetOf() }.add(child)
    }

    fun detach(parent: StateContext?, child: StateContext) {
        parent ?: return
        childrenMap[parent]?.remove(child)
        if (childrenMap[parent]?.isEmpty() == true) {
            childrenMap.remove(parent)
        }
    }

    fun childrenOf(ctx: StateContext): Set<StateContext> =
        childrenMap[ctx] ?: emptySet()
}