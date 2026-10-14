package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered trailer yard metrics by code. */
interface TieredTrailerYardMetricStore {
    fun find(code: String): TieredTrailerYardMetric?
    fun save(item: TieredTrailerYardMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTrailerYardMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTrailerYardMetricStore(
    var items: MutableMap<String, TieredTrailerYardMetric>,
    var order: MutableList<String>,
) : TieredTrailerYardMetricStore {
    override fun find(code: String): TieredTrailerYardMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTrailerYardMetric) {
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

    override fun list(): MutableList<TieredTrailerYardMetric> {
        val out = mutableListOf<TieredTrailerYardMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTrailerYardMetricStore(): TieredTrailerYardMetricStore {
    return MemoryTieredTrailerYardMetricStore(
        mutableMapOf<String, TieredTrailerYardMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTrailerYardMetricStore(
    var inner: TieredTrailerYardMetricStore,
    var cache: TtlCache<TieredTrailerYardMetric>,
    var hits: Int,
    var misses: Int,
) : TieredTrailerYardMetricStore {
    override fun find(code: String): TieredTrailerYardMetric? {
        val hit = (cache.get(code) as TieredTrailerYardMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTrailerYardMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTrailerYardMetric> = inner.list()
}

fun newCachedTieredTrailerYardMetricStore(inner: TieredTrailerYardMetricStore, ttl: Int, clock: Clock): TieredTrailerYardMetricStore {
    return CachedTieredTrailerYardMetricStore(
        inner = inner,
        cache = newTtlCache<TieredTrailerYardMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered trailer yard metrics. Empty fields match all. */
data class TieredTrailerYardMetricQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTrailerYardMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredTrailerYardMetric>): MutableList<TieredTrailerYardMetric> {
        val matched = filter(items) { candidate: TieredTrailerYardMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTrailerYardMetrics(store: TieredTrailerYardMetricStore, query: TieredTrailerYardMetricQuery): MutableList<TieredTrailerYardMetric> {
    return query.apply(store.list())
}
