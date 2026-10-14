package com.acme.transport.network.node.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node cycles by code. */
interface NodeCycleStore {
    fun find(code: String): NodeCycle?
    fun save(item: NodeCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeCycleStore(
    var items: MutableMap<String, NodeCycle>,
    var order: MutableList<String>,
) : NodeCycleStore {
    override fun find(code: String): NodeCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeCycle) {
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

    override fun list(): MutableList<NodeCycle> {
        val out = mutableListOf<NodeCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeCycleStore(): NodeCycleStore {
    return MemoryNodeCycleStore(mutableMapOf<String, NodeCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeCycleStore(
    var inner: NodeCycleStore,
    var cache: TtlCache<NodeCycle>,
    var hits: Int,
    var misses: Int,
) : NodeCycleStore {
    override fun find(code: String): NodeCycle? {
        val hit = (cache.get(code) as NodeCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeCycle> = inner.list()
}

fun newCachedNodeCycleStore(inner: NodeCycleStore, ttl: Int, clock: Clock): NodeCycleStore {
    return CachedNodeCycleStore(
        inner = inner,
        cache = newTtlCache<NodeCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node cycles. Empty fields match all. */
data class NodeCycleQuery(
    var unit: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeCycle): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeCycle>): MutableList<NodeCycle> {
        val matched = filter(items) { candidate: NodeCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeCycles(store: NodeCycleStore, query: NodeCycleQuery): MutableList<NodeCycle> {
    return query.apply(store.list())
}
