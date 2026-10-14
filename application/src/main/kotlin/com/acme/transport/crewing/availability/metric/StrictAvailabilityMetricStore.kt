package com.acme.transport.crewing.availability.metric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict availability metrics by code. */
interface StrictAvailabilityMetricStore {
    fun find(code: String): StrictAvailabilityMetric?
    fun save(item: StrictAvailabilityMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAvailabilityMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAvailabilityMetricStore(
    var items: MutableMap<String, StrictAvailabilityMetric>,
    var order: MutableList<String>,
) : StrictAvailabilityMetricStore {
    override fun find(code: String): StrictAvailabilityMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAvailabilityMetric) {
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

    override fun list(): MutableList<StrictAvailabilityMetric> {
        val out = mutableListOf<StrictAvailabilityMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAvailabilityMetricStore(): StrictAvailabilityMetricStore {
    return MemoryStrictAvailabilityMetricStore(
        mutableMapOf<String, StrictAvailabilityMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAvailabilityMetricStore(
    var inner: StrictAvailabilityMetricStore,
    var cache: TtlCache<StrictAvailabilityMetric>,
    var hits: Int,
    var misses: Int,
) : StrictAvailabilityMetricStore {
    override fun find(code: String): StrictAvailabilityMetric? {
        val hit = (cache.get(code) as StrictAvailabilityMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAvailabilityMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAvailabilityMetric> = inner.list()
}

fun newCachedStrictAvailabilityMetricStore(inner: StrictAvailabilityMetricStore, ttl: Int, clock: Clock): StrictAvailabilityMetricStore {
    return CachedStrictAvailabilityMetricStore(
        inner = inner,
        cache = newTtlCache<StrictAvailabilityMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict availability metrics. Empty fields match all. */
data class StrictAvailabilityMetricQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAvailabilityMetric): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictAvailabilityMetric>): MutableList<StrictAvailabilityMetric> {
        val matched = filter(items) { candidate: StrictAvailabilityMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAvailabilityMetrics(store: StrictAvailabilityMetricStore, query: StrictAvailabilityMetricQuery): MutableList<StrictAvailabilityMetric> {
    return query.apply(store.list())
}
