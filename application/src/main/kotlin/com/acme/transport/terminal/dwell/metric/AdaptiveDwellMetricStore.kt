package com.acme.transport.terminal.dwell.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive dwell metrics by code. */
interface AdaptiveDwellMetricStore {
    fun find(code: String): AdaptiveDwellMetric?
    fun save(item: AdaptiveDwellMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDwellMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDwellMetricStore(
    var items: MutableMap<String, AdaptiveDwellMetric>,
    var order: MutableList<String>,
) : AdaptiveDwellMetricStore {
    override fun find(code: String): AdaptiveDwellMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDwellMetric) {
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

    override fun list(): MutableList<AdaptiveDwellMetric> {
        val out = mutableListOf<AdaptiveDwellMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDwellMetricStore(): AdaptiveDwellMetricStore {
    return MemoryAdaptiveDwellMetricStore(mutableMapOf<String, AdaptiveDwellMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDwellMetricStore(
    var inner: AdaptiveDwellMetricStore,
    var cache: TtlCache<AdaptiveDwellMetric>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDwellMetricStore {
    override fun find(code: String): AdaptiveDwellMetric? {
        val hit = (cache.get(code) as AdaptiveDwellMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDwellMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDwellMetric> = inner.list()
}

fun newCachedAdaptiveDwellMetricStore(inner: AdaptiveDwellMetricStore, ttl: Int, clock: Clock): AdaptiveDwellMetricStore {
    return CachedAdaptiveDwellMetricStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDwellMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive dwell metrics. Empty fields match all. */
data class AdaptiveDwellMetricQuery(
    var depot: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDwellMetric): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDwellMetric>): MutableList<AdaptiveDwellMetric> {
        val matched = filter(items) { candidate: AdaptiveDwellMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDwellMetrics(store: AdaptiveDwellMetricStore, query: AdaptiveDwellMetricQuery): MutableList<AdaptiveDwellMetric> {
    return query.apply(store.list())
}
