package com.acme.transport.network.node.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node metrics by code. */
interface NodeMetricStore {
    fun find(code: String): NodeMetric?
    fun save(item: NodeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeMetricStore(
    var items: MutableMap<String, NodeMetric>,
    var order: MutableList<String>,
) : NodeMetricStore {
    override fun find(code: String): NodeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeMetric) {
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

    override fun list(): MutableList<NodeMetric> {
        val out = mutableListOf<NodeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeMetricStore(): NodeMetricStore {
    return MemoryNodeMetricStore(mutableMapOf<String, NodeMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeMetricStore(
    var inner: NodeMetricStore,
    var cache: TtlCache<NodeMetric>,
    var hits: Int,
    var misses: Int,
) : NodeMetricStore {
    override fun find(code: String): NodeMetric? {
        val hit = (cache.get(code) as NodeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeMetric> = inner.list()
}

fun newCachedNodeMetricStore(inner: NodeMetricStore, ttl: Int, clock: Clock): NodeMetricStore {
    return CachedNodeMetricStore(
        inner = inner,
        cache = newTtlCache<NodeMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node metrics. Empty fields match all. */
data class NodeMetricQuery(
    var locale: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeMetric): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<NodeMetric>): MutableList<NodeMetric> {
        val matched = filter(items) { candidate: NodeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeMetrics(store: NodeMetricStore, query: NodeMetricQuery): MutableList<NodeMetric> {
    return query.apply(store.list())
}
