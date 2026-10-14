package com.acme.transport.customs.broker.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered broker metrics by code. */
interface TieredBrokerMetricStore {
    fun find(code: String): TieredBrokerMetric?
    fun save(item: TieredBrokerMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredBrokerMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredBrokerMetricStore(
    var items: MutableMap<String, TieredBrokerMetric>,
    var order: MutableList<String>,
) : TieredBrokerMetricStore {
    override fun find(code: String): TieredBrokerMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredBrokerMetric) {
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

    override fun list(): MutableList<TieredBrokerMetric> {
        val out = mutableListOf<TieredBrokerMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredBrokerMetricStore(): TieredBrokerMetricStore {
    return MemoryTieredBrokerMetricStore(mutableMapOf<String, TieredBrokerMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredBrokerMetricStore(
    var inner: TieredBrokerMetricStore,
    var cache: TtlCache<TieredBrokerMetric>,
    var hits: Int,
    var misses: Int,
) : TieredBrokerMetricStore {
    override fun find(code: String): TieredBrokerMetric? {
        val hit = (cache.get(code) as TieredBrokerMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredBrokerMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredBrokerMetric> = inner.list()
}

fun newCachedTieredBrokerMetricStore(inner: TieredBrokerMetricStore, ttl: Int, clock: Clock): TieredBrokerMetricStore {
    return CachedTieredBrokerMetricStore(
        inner = inner,
        cache = newTtlCache<TieredBrokerMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered broker metrics. Empty fields match all. */
data class TieredBrokerMetricQuery(
    var source: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredBrokerMetric): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<TieredBrokerMetric>): MutableList<TieredBrokerMetric> {
        val matched = filter(items) { candidate: TieredBrokerMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredBrokerMetrics(store: TieredBrokerMetricStore, query: TieredBrokerMetricQuery): MutableList<TieredBrokerMetric> {
    return query.apply(store.list())
}
