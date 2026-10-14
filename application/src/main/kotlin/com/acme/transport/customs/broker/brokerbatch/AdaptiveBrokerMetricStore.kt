package com.acme.transport.customs.broker.brokerbatch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive broker metrics by code. */
interface AdaptiveBrokerMetricStore {
    fun find(code: String): AdaptiveBrokerMetric?
    fun save(item: AdaptiveBrokerMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveBrokerMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveBrokerMetricStore(
    var items: MutableMap<String, AdaptiveBrokerMetric>,
    var order: MutableList<String>,
) : AdaptiveBrokerMetricStore {
    override fun find(code: String): AdaptiveBrokerMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveBrokerMetric) {
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

    override fun list(): MutableList<AdaptiveBrokerMetric> {
        val out = mutableListOf<AdaptiveBrokerMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveBrokerMetricStore(): AdaptiveBrokerMetricStore {
    return MemoryAdaptiveBrokerMetricStore(
        mutableMapOf<String, AdaptiveBrokerMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveBrokerMetricStore(
    var inner: AdaptiveBrokerMetricStore,
    var cache: TtlCache<AdaptiveBrokerMetric>,
    var hits: Int,
    var misses: Int,
) : AdaptiveBrokerMetricStore {
    override fun find(code: String): AdaptiveBrokerMetric? {
        val hit = (cache.get(code) as AdaptiveBrokerMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveBrokerMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveBrokerMetric> = inner.list()
}

fun newCachedAdaptiveBrokerMetricStore(inner: AdaptiveBrokerMetricStore, ttl: Int, clock: Clock): AdaptiveBrokerMetricStore {
    return CachedAdaptiveBrokerMetricStore(
        inner = inner,
        cache = newTtlCache<AdaptiveBrokerMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive broker metrics. Empty fields match all. */
data class AdaptiveBrokerMetricQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveBrokerMetric): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveBrokerMetric>): MutableList<AdaptiveBrokerMetric> {
        val matched = filter(items) { candidate: AdaptiveBrokerMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveBrokerMetrics(store: AdaptiveBrokerMetricStore, query: AdaptiveBrokerMetricQuery): MutableList<AdaptiveBrokerMetric> {
    return query.apply(store.list())
}
