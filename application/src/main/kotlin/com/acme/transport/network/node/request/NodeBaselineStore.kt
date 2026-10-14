package com.acme.transport.network.node.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node baselines by code. */
interface NodeBaselineStore {
    fun find(code: String): NodeBaseline?
    fun save(item: NodeBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeBaselineStore(
    var items: MutableMap<String, NodeBaseline>,
    var order: MutableList<String>,
) : NodeBaselineStore {
    override fun find(code: String): NodeBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeBaseline) {
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

    override fun list(): MutableList<NodeBaseline> {
        val out = mutableListOf<NodeBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeBaselineStore(): NodeBaselineStore {
    return MemoryNodeBaselineStore(mutableMapOf<String, NodeBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeBaselineStore(
    var inner: NodeBaselineStore,
    var cache: TtlCache<NodeBaseline>,
    var hits: Int,
    var misses: Int,
) : NodeBaselineStore {
    override fun find(code: String): NodeBaseline? {
        val hit = (cache.get(code) as NodeBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeBaseline> = inner.list()
}

fun newCachedNodeBaselineStore(inner: NodeBaselineStore, ttl: Int, clock: Clock): NodeBaselineStore {
    return CachedNodeBaselineStore(
        inner = inner,
        cache = newTtlCache<NodeBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node baselines. Empty fields match all. */
data class NodeBaselineQuery(
    var channel: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeBaseline): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeBaseline>): MutableList<NodeBaseline> {
        val matched = filter(items) { candidate: NodeBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeBaselines(store: NodeBaselineStore, query: NodeBaselineQuery): MutableList<NodeBaseline> {
    return query.apply(store.list())
}
