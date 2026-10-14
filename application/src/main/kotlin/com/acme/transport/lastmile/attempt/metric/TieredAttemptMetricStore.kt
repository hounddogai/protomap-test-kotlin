package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered attempt metrics by code. */
interface TieredAttemptMetricStore {
    fun find(code: String): TieredAttemptMetric?
    fun save(item: TieredAttemptMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredAttemptMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredAttemptMetricStore(
    var items: MutableMap<String, TieredAttemptMetric>,
    var order: MutableList<String>,
) : TieredAttemptMetricStore {
    override fun find(code: String): TieredAttemptMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredAttemptMetric) {
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

    override fun list(): MutableList<TieredAttemptMetric> {
        val out = mutableListOf<TieredAttemptMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredAttemptMetricStore(): TieredAttemptMetricStore {
    return MemoryTieredAttemptMetricStore(mutableMapOf<String, TieredAttemptMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredAttemptMetricStore(
    var inner: TieredAttemptMetricStore,
    var cache: TtlCache<TieredAttemptMetric>,
    var hits: Int,
    var misses: Int,
) : TieredAttemptMetricStore {
    override fun find(code: String): TieredAttemptMetric? {
        val hit = (cache.get(code) as TieredAttemptMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredAttemptMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredAttemptMetric> = inner.list()
}

fun newCachedTieredAttemptMetricStore(inner: TieredAttemptMetricStore, ttl: Int, clock: Clock): TieredAttemptMetricStore {
    return CachedTieredAttemptMetricStore(
        inner = inner,
        cache = newTtlCache<TieredAttemptMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered attempt metrics. Empty fields match all. */
data class TieredAttemptMetricQuery(
    var country: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredAttemptMetric): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredAttemptMetric>): MutableList<TieredAttemptMetric> {
        val matched = filter(items) { candidate: TieredAttemptMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredAttemptMetrics(store: TieredAttemptMetricStore, query: TieredAttemptMetricQuery): MutableList<TieredAttemptMetric> {
    return query.apply(store.list())
}
