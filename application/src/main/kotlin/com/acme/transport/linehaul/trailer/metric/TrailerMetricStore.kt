package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer metrics by code. */
interface TrailerMetricStore {
    fun find(code: String): TrailerMetric?
    fun save(item: TrailerMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerMetricStore(
    var items: MutableMap<String, TrailerMetric>,
    var order: MutableList<String>,
) : TrailerMetricStore {
    override fun find(code: String): TrailerMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerMetric) {
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

    override fun list(): MutableList<TrailerMetric> {
        val out = mutableListOf<TrailerMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerMetricStore(): TrailerMetricStore {
    return MemoryTrailerMetricStore(mutableMapOf<String, TrailerMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerMetricStore(
    var inner: TrailerMetricStore,
    var cache: TtlCache<TrailerMetric>,
    var hits: Int,
    var misses: Int,
) : TrailerMetricStore {
    override fun find(code: String): TrailerMetric? {
        val hit = (cache.get(code) as TrailerMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerMetric> = inner.list()
}

fun newCachedTrailerMetricStore(inner: TrailerMetricStore, ttl: Int, clock: Clock): TrailerMetricStore {
    return CachedTrailerMetricStore(
        inner = inner,
        cache = newTtlCache<TrailerMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer metrics. Empty fields match all. */
data class TrailerMetricQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerMetric): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TrailerMetric>): MutableList<TrailerMetric> {
        val matched = filter(items) { candidate: TrailerMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerMetrics(store: TrailerMetricStore, query: TrailerMetricQuery): MutableList<TrailerMetric> {
    return query.apply(store.list())
}
