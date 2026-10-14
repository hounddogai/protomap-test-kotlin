package com.acme.transport.lastmile.attempt.attemptmetric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional attempt metrics by code. */
interface RegionalAttemptMetricStore {
    fun find(code: String): RegionalAttemptMetric?
    fun save(item: RegionalAttemptMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalAttemptMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalAttemptMetricStore(
    var items: MutableMap<String, RegionalAttemptMetric>,
    var order: MutableList<String>,
) : RegionalAttemptMetricStore {
    override fun find(code: String): RegionalAttemptMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalAttemptMetric) {
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

    override fun list(): MutableList<RegionalAttemptMetric> {
        val out = mutableListOf<RegionalAttemptMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalAttemptMetricStore(): RegionalAttemptMetricStore {
    return MemoryRegionalAttemptMetricStore(
        mutableMapOf<String, RegionalAttemptMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalAttemptMetricStore(
    var inner: RegionalAttemptMetricStore,
    var cache: TtlCache<RegionalAttemptMetric>,
    var hits: Int,
    var misses: Int,
) : RegionalAttemptMetricStore {
    override fun find(code: String): RegionalAttemptMetric? {
        val hit = (cache.get(code) as RegionalAttemptMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalAttemptMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalAttemptMetric> = inner.list()
}

fun newCachedRegionalAttemptMetricStore(inner: RegionalAttemptMetricStore, ttl: Int, clock: Clock): RegionalAttemptMetricStore {
    return CachedRegionalAttemptMetricStore(
        inner = inner,
        cache = newTtlCache<RegionalAttemptMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional attempt metrics. Empty fields match all. */
data class RegionalAttemptMetricQuery(
    var locale: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalAttemptMetric): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalAttemptMetric>): MutableList<RegionalAttemptMetric> {
        val matched = filter(items) { candidate: RegionalAttemptMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalAttemptMetrics(store: RegionalAttemptMetricStore, query: RegionalAttemptMetricQuery): MutableList<RegionalAttemptMetric> {
    return query.apply(store.list())
}
