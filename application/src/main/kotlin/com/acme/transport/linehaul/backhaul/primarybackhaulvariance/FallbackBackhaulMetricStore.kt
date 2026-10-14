package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback backhaul metrics by code. */
interface FallbackBackhaulMetricStore {
    fun find(code: String): FallbackBackhaulMetric?
    fun save(item: FallbackBackhaulMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackBackhaulMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackBackhaulMetricStore(
    var items: MutableMap<String, FallbackBackhaulMetric>,
    var order: MutableList<String>,
) : FallbackBackhaulMetricStore {
    override fun find(code: String): FallbackBackhaulMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackBackhaulMetric) {
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

    override fun list(): MutableList<FallbackBackhaulMetric> {
        val out = mutableListOf<FallbackBackhaulMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackBackhaulMetricStore(): FallbackBackhaulMetricStore {
    return MemoryFallbackBackhaulMetricStore(
        mutableMapOf<String, FallbackBackhaulMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackBackhaulMetricStore(
    var inner: FallbackBackhaulMetricStore,
    var cache: TtlCache<FallbackBackhaulMetric>,
    var hits: Int,
    var misses: Int,
) : FallbackBackhaulMetricStore {
    override fun find(code: String): FallbackBackhaulMetric? {
        val hit = (cache.get(code) as FallbackBackhaulMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackBackhaulMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackBackhaulMetric> = inner.list()
}

fun newCachedFallbackBackhaulMetricStore(inner: FallbackBackhaulMetricStore, ttl: Int, clock: Clock): FallbackBackhaulMetricStore {
    return CachedFallbackBackhaulMetricStore(
        inner = inner,
        cache = newTtlCache<FallbackBackhaulMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback backhaul metrics. Empty fields match all. */
data class FallbackBackhaulMetricQuery(
    var unit: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackBackhaulMetric): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackBackhaulMetric>): MutableList<FallbackBackhaulMetric> {
        val matched = filter(items) { candidate: FallbackBackhaulMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackBackhaulMetrics(store: FallbackBackhaulMetricStore, query: FallbackBackhaulMetricQuery): MutableList<FallbackBackhaulMetric> {
    return query.apply(store.list())
}
