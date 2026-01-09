package com.digia.digiaui.framework.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf

class StateContext(
    val namespace: String? = null,
    val parent: StateContext? = null,
    private val tree: StateTree,
    initialState: Map<String, Any?> = emptyMap()
) {

    private class Entry(
        var value: Any?,
        val version: MutableState<Int> = mutableIntStateOf(0)
    )

    private val entries = mutableMapOf<String, Entry>()
    private val dirtyEntries = mutableSetOf<Entry>()

    init {
        initialState.forEach { (k, v) ->
            entries[k] = Entry(v)
        }
        tree.attach(parent, this)
    }

    fun dispose() {
        tree.childrenOf(this).forEach { it.dispose() }
        tree.detach(parent, this)
    }

    /* -------- Reads -------- */

    fun get(key: String): Any? =
        entries[key]?.value ?: parent?.get(key)

    @Composable
    fun observe(key: String): Any? {
        val entry = entries[key]
        if (entry != null) {
            entry.version.value
            return entry.value
        }
        return parent?.observe(key)
    }

    /* -------- Writes -------- */

    fun set(key: String, value: Any?, notify: Boolean = true) {
        val entry = entries.getOrPut(key) { Entry(null) }
        entry.value = value
        if (notify) entry.version.value++
        else dirtyEntries.add(entry)
    }

   private fun flush() {
        dirtyEntries.forEach { it.version.value++ }
        dirtyEntries.clear()

        // 🔥 Downward propagation handled by tree
        tree.childrenOf(this).forEach { it.flush() }
    }


    /* -------- Utilities -------- */
    fun snapshot(): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        entries.forEach { (k, v) ->
            result[k] = v.value
        }
        return result
    }


    fun containsKey(key: String): Boolean =
        entries.containsKey(key) || parent?.containsKey(key) == true
}




