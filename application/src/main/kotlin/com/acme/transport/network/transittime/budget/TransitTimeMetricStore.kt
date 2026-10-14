package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time metrics by code. */
interface TransitTimeMetricStore {
    fun find(code: String): TransitTimeMetric?
    fun save(item: TransitTimeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeMetricStore(
    var items: MutableMap<String, TransitTimeMetric>,
    var order: MutableList<String>,
) : TransitTimeMetricStore {
    override fun find(code: String): TransitTimeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeMetric) {
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

    override fun list(): MutableList<TransitTimeMetric> {
        val out = mutableListOf<TransitTimeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeMetricStore(): TransitTimeMetricStore {
    return MemoryTransitTimeMetricStore(mutableMapOf<String, TransitTimeMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeMetricStore(
    var inner: TransitTimeMetricStore,
    var cache: TtlCache<TransitTimeMetric>,
    var hits: Int,
    var misses: Int,
) : TransitTimeMetricStore {
    override fun find(code: String): TransitTimeMetric? {
        val hit = (cache.get(code) as TransitTimeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeMetric> = inner.list()
}

fun newCachedTransitTimeMetricStore(inner: TransitTimeMetricStore, ttl: Int, clock: Clock): TransitTimeMetricStore {
    return CachedTransitTimeMetricStore(
        inner = inner,
        cache = newTtlCache<TransitTimeMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time metrics. Empty fields match all. */
data class TransitTimeMetricQuery(
    var region: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeMetric): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeMetric>): MutableList<TransitTimeMetric> {
        val matched = filter(items) { candidate: TransitTimeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeMetrics(store: TransitTimeMetricStore, query: TransitTimeMetricQuery): MutableList<TransitTimeMetric> {
    return query.apply(store.list())
}
