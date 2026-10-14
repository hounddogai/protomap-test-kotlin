package com.acme.transport.customs.broker.metric

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional broker metrics by code. */
interface RegionalBrokerMetricStore {
    fun find(code: String): RegionalBrokerMetric?
    fun save(item: RegionalBrokerMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalBrokerMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalBrokerMetricStore(
    var items: MutableMap<String, RegionalBrokerMetric>,
    var order: MutableList<String>,
) : RegionalBrokerMetricStore {
    override fun find(code: String): RegionalBrokerMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalBrokerMetric) {
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

    override fun list(): MutableList<RegionalBrokerMetric> {
        val out = mutableListOf<RegionalBrokerMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalBrokerMetricStore(): RegionalBrokerMetricStore {
    return MemoryRegionalBrokerMetricStore(
        mutableMapOf<String, RegionalBrokerMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalBrokerMetricStore(
    var inner: RegionalBrokerMetricStore,
    var cache: TtlCache<RegionalBrokerMetric>,
    var hits: Int,
    var misses: Int,
) : RegionalBrokerMetricStore {
    override fun find(code: String): RegionalBrokerMetric? {
        val hit = (cache.get(code) as RegionalBrokerMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalBrokerMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalBrokerMetric> = inner.list()
}

fun newCachedRegionalBrokerMetricStore(inner: RegionalBrokerMetricStore, ttl: Int, clock: Clock): RegionalBrokerMetricStore {
    return CachedRegionalBrokerMetricStore(
        inner = inner,
        cache = newTtlCache<RegionalBrokerMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional broker metrics. Empty fields match all. */
data class RegionalBrokerMetricQuery(
    var category: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalBrokerMetric): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalBrokerMetric>): MutableList<RegionalBrokerMetric> {
        val matched = filter(items) { candidate: RegionalBrokerMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalBrokerMetrics(store: RegionalBrokerMetricStore, query: RegionalBrokerMetricQuery): MutableList<RegionalBrokerMetric> {
    return query.apply(store.list())
}
