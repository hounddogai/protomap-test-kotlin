package com.acme.transport.terminal.traileryard.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard metrics by code. */
interface TrailerYardMetricStore {
    fun find(code: String): TrailerYardMetric?
    fun save(item: TrailerYardMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardMetricStore(
    var items: MutableMap<String, TrailerYardMetric>,
    var order: MutableList<String>,
) : TrailerYardMetricStore {
    override fun find(code: String): TrailerYardMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardMetric) {
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

    override fun list(): MutableList<TrailerYardMetric> {
        val out = mutableListOf<TrailerYardMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardMetricStore(): TrailerYardMetricStore {
    return MemoryTrailerYardMetricStore(mutableMapOf<String, TrailerYardMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardMetricStore(
    var inner: TrailerYardMetricStore,
    var cache: TtlCache<TrailerYardMetric>,
    var hits: Int,
    var misses: Int,
) : TrailerYardMetricStore {
    override fun find(code: String): TrailerYardMetric? {
        val hit = (cache.get(code) as TrailerYardMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardMetric> = inner.list()
}

fun newCachedTrailerYardMetricStore(inner: TrailerYardMetricStore, ttl: Int, clock: Clock): TrailerYardMetricStore {
    return CachedTrailerYardMetricStore(
        inner = inner,
        cache = newTtlCache<TrailerYardMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard metrics. Empty fields match all. */
data class TrailerYardMetricQuery(
    var category: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardMetric): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardMetric>): MutableList<TrailerYardMetric> {
        val matched = filter(items) { candidate: TrailerYardMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardMetrics(store: TrailerYardMetricStore, query: TrailerYardMetricQuery): MutableList<TrailerYardMetric> {
    return query.apply(store.list())
}
