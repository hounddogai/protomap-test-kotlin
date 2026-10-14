package com.acme.transport.network.node.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback node metrics by code. */
interface FallbackNodeMetricStore {
    fun find(code: String): FallbackNodeMetric?
    fun save(item: FallbackNodeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackNodeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackNodeMetricStore(
    var items: MutableMap<String, FallbackNodeMetric>,
    var order: MutableList<String>,
) : FallbackNodeMetricStore {
    override fun find(code: String): FallbackNodeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackNodeMetric) {
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

    override fun list(): MutableList<FallbackNodeMetric> {
        val out = mutableListOf<FallbackNodeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackNodeMetricStore(): FallbackNodeMetricStore {
    return MemoryFallbackNodeMetricStore(mutableMapOf<String, FallbackNodeMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackNodeMetricStore(
    var inner: FallbackNodeMetricStore,
    var cache: TtlCache<FallbackNodeMetric>,
    var hits: Int,
    var misses: Int,
) : FallbackNodeMetricStore {
    override fun find(code: String): FallbackNodeMetric? {
        val hit = (cache.get(code) as FallbackNodeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackNodeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackNodeMetric> = inner.list()
}

fun newCachedFallbackNodeMetricStore(inner: FallbackNodeMetricStore, ttl: Int, clock: Clock): FallbackNodeMetricStore {
    return CachedFallbackNodeMetricStore(
        inner = inner,
        cache = newTtlCache<FallbackNodeMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback node metrics. Empty fields match all. */
data class FallbackNodeMetricQuery(
    var lane: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackNodeMetric): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackNodeMetric>): MutableList<FallbackNodeMetric> {
        val matched = filter(items) { candidate: FallbackNodeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackNodeMetrics(store: FallbackNodeMetricStore, query: FallbackNodeMetricQuery): MutableList<FallbackNodeMetric> {
    return query.apply(store.list())
}
