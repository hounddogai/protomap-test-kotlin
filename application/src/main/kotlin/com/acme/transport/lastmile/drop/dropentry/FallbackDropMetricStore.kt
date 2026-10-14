package com.acme.transport.lastmile.drop.dropentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback drop metrics by code. */
interface FallbackDropMetricStore {
    fun find(code: String): FallbackDropMetric?
    fun save(item: FallbackDropMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDropMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDropMetricStore(
    var items: MutableMap<String, FallbackDropMetric>,
    var order: MutableList<String>,
) : FallbackDropMetricStore {
    override fun find(code: String): FallbackDropMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDropMetric) {
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

    override fun list(): MutableList<FallbackDropMetric> {
        val out = mutableListOf<FallbackDropMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDropMetricStore(): FallbackDropMetricStore {
    return MemoryFallbackDropMetricStore(mutableMapOf<String, FallbackDropMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDropMetricStore(
    var inner: FallbackDropMetricStore,
    var cache: TtlCache<FallbackDropMetric>,
    var hits: Int,
    var misses: Int,
) : FallbackDropMetricStore {
    override fun find(code: String): FallbackDropMetric? {
        val hit = (cache.get(code) as FallbackDropMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDropMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDropMetric> = inner.list()
}

fun newCachedFallbackDropMetricStore(inner: FallbackDropMetricStore, ttl: Int, clock: Clock): FallbackDropMetricStore {
    return CachedFallbackDropMetricStore(
        inner = inner,
        cache = newTtlCache<FallbackDropMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback drop metrics. Empty fields match all. */
data class FallbackDropMetricQuery(
    var country: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDropMetric): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDropMetric>): MutableList<FallbackDropMetric> {
        val matched = filter(items) { candidate: FallbackDropMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDropMetrics(store: FallbackDropMetricStore, query: FallbackDropMetricQuery): MutableList<FallbackDropMetric> {
    return query.apply(store.list())
}
