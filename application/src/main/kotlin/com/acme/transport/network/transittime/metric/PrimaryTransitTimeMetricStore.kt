package com.acme.transport.network.transittime.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary transit time metrics by code. */
interface PrimaryTransitTimeMetricStore {
    fun find(code: String): PrimaryTransitTimeMetric?
    fun save(item: PrimaryTransitTimeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTransitTimeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTransitTimeMetricStore(
    var items: MutableMap<String, PrimaryTransitTimeMetric>,
    var order: MutableList<String>,
) : PrimaryTransitTimeMetricStore {
    override fun find(code: String): PrimaryTransitTimeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTransitTimeMetric) {
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

    override fun list(): MutableList<PrimaryTransitTimeMetric> {
        val out = mutableListOf<PrimaryTransitTimeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTransitTimeMetricStore(): PrimaryTransitTimeMetricStore {
    return MemoryPrimaryTransitTimeMetricStore(
        mutableMapOf<String, PrimaryTransitTimeMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTransitTimeMetricStore(
    var inner: PrimaryTransitTimeMetricStore,
    var cache: TtlCache<PrimaryTransitTimeMetric>,
    var hits: Int,
    var misses: Int,
) : PrimaryTransitTimeMetricStore {
    override fun find(code: String): PrimaryTransitTimeMetric? {
        val hit = (cache.get(code) as PrimaryTransitTimeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTransitTimeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTransitTimeMetric> = inner.list()
}

fun newCachedPrimaryTransitTimeMetricStore(inner: PrimaryTransitTimeMetricStore, ttl: Int, clock: Clock): PrimaryTransitTimeMetricStore {
    return CachedPrimaryTransitTimeMetricStore(
        inner = inner,
        cache = newTtlCache<PrimaryTransitTimeMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary transit time metrics. Empty fields match all. */
data class PrimaryTransitTimeMetricQuery(
    var country: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTransitTimeMetric): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTransitTimeMetric>): MutableList<PrimaryTransitTimeMetric> {
        val matched = filter(items) { candidate: PrimaryTransitTimeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTransitTimeMetrics(store: PrimaryTransitTimeMetricStore, query: PrimaryTransitTimeMetricQuery): MutableList<PrimaryTransitTimeMetric> {
    return query.apply(store.list())
}
