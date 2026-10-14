package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node estimates by code. */
interface NodeEstimateStore {
    fun find(code: String): NodeEstimate?
    fun save(item: NodeEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeEstimateStore(
    var items: MutableMap<String, NodeEstimate>,
    var order: MutableList<String>,
) : NodeEstimateStore {
    override fun find(code: String): NodeEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeEstimate) {
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

    override fun list(): MutableList<NodeEstimate> {
        val out = mutableListOf<NodeEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeEstimateStore(): NodeEstimateStore {
    return MemoryNodeEstimateStore(mutableMapOf<String, NodeEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeEstimateStore(
    var inner: NodeEstimateStore,
    var cache: TtlCache<NodeEstimate>,
    var hits: Int,
    var misses: Int,
) : NodeEstimateStore {
    override fun find(code: String): NodeEstimate? {
        val hit = (cache.get(code) as NodeEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeEstimate> = inner.list()
}

fun newCachedNodeEstimateStore(inner: NodeEstimateStore, ttl: Int, clock: Clock): NodeEstimateStore {
    return CachedNodeEstimateStore(
        inner = inner,
        cache = newTtlCache<NodeEstimate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node estimates. Empty fields match all. */
data class NodeEstimateQuery(
    var channel: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeEstimate): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeEstimate>): MutableList<NodeEstimate> {
        val matched = filter(items) { candidate: NodeEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeEstimates(store: NodeEstimateStore, query: NodeEstimateQuery): MutableList<NodeEstimate> {
    return query.apply(store.list())
}
