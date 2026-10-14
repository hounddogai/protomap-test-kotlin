package com.acme.transport.network.node.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node windows by code. */
interface NodeWindowStore {
    fun find(code: String): NodeWindow?
    fun save(item: NodeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeWindowStore(
    var items: MutableMap<String, NodeWindow>,
    var order: MutableList<String>,
) : NodeWindowStore {
    override fun find(code: String): NodeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeWindow) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<NodeWindow> {
        val out = mutableListOf<NodeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeWindowStore(): NodeWindowStore {
    return MemoryNodeWindowStore(mutableMapOf<String, NodeWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeWindowStore(
    var inner: NodeWindowStore,
    var cache: TtlCache<NodeWindow>,
    var hits: Int,
    var misses: Int,
) : NodeWindowStore {
    override fun find(code: String): NodeWindow? {
        val hit = (cache.get(code) as NodeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeWindow> = inner.list()
}

fun newCachedNodeWindowStore(inner: NodeWindowStore, ttl: Int, clock: Clock): NodeWindowStore {
    return CachedNodeWindowStore(
        inner = inner,
        cache = newTtlCache<NodeWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node windows. Empty fields match all. */
data class NodeWindowQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeWindow>): MutableList<NodeWindow> {
        val matched = filter(items) { candidate: NodeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeWindows(store: NodeWindowStore, query: NodeWindowQuery): MutableList<NodeWindow> {
    return query.apply(store.list())
}
