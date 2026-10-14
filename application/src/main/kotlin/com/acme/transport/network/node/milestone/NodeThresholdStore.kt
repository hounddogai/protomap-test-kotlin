package com.acme.transport.network.node.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node thresholds by code. */
interface NodeThresholdStore {
    fun find(code: String): NodeThreshold?
    fun save(item: NodeThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeThresholdStore(
    var items: MutableMap<String, NodeThreshold>,
    var order: MutableList<String>,
) : NodeThresholdStore {
    override fun find(code: String): NodeThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeThreshold) {
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

    override fun list(): MutableList<NodeThreshold> {
        val out = mutableListOf<NodeThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeThresholdStore(): NodeThresholdStore {
    return MemoryNodeThresholdStore(mutableMapOf<String, NodeThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeThresholdStore(
    var inner: NodeThresholdStore,
    var cache: TtlCache<NodeThreshold>,
    var hits: Int,
    var misses: Int,
) : NodeThresholdStore {
    override fun find(code: String): NodeThreshold? {
        val hit = (cache.get(code) as NodeThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeThreshold> = inner.list()
}

fun newCachedNodeThresholdStore(inner: NodeThresholdStore, ttl: Int, clock: Clock): NodeThresholdStore {
    return CachedNodeThresholdStore(
        inner = inner,
        cache = newTtlCache<NodeThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node thresholds. Empty fields match all. */
data class NodeThresholdQuery(
    var unit: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeThreshold): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeThreshold>): MutableList<NodeThreshold> {
        val matched = filter(items) { candidate: NodeThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeThresholds(store: NodeThresholdStore, query: NodeThresholdQuery): MutableList<NodeThreshold> {
    return query.apply(store.list())
}
