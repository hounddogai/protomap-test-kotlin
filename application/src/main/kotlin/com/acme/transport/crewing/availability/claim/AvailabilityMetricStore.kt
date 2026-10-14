package com.acme.transport.crewing.availability.claim

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability metrics by code. */
interface AvailabilityMetricStore {
    fun find(code: String): AvailabilityMetric?
    fun save(item: AvailabilityMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityMetricStore(
    var items: MutableMap<String, AvailabilityMetric>,
    var order: MutableList<String>,
) : AvailabilityMetricStore {
    override fun find(code: String): AvailabilityMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityMetric) {
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

    override fun list(): MutableList<AvailabilityMetric> {
        val out = mutableListOf<AvailabilityMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityMetricStore(): AvailabilityMetricStore {
    return MemoryAvailabilityMetricStore(mutableMapOf<String, AvailabilityMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityMetricStore(
    var inner: AvailabilityMetricStore,
    var cache: TtlCache<AvailabilityMetric>,
    var hits: Int,
    var misses: Int,
) : AvailabilityMetricStore {
    override fun find(code: String): AvailabilityMetric? {
        val hit = (cache.get(code) as AvailabilityMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityMetric> = inner.list()
}

fun newCachedAvailabilityMetricStore(inner: AvailabilityMetricStore, ttl: Int, clock: Clock): AvailabilityMetricStore {
    return CachedAvailabilityMetricStore(
        inner = inner,
        cache = newTtlCache<AvailabilityMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability metrics. Empty fields match all. */
data class AvailabilityMetricQuery(
    var source: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityMetric): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<AvailabilityMetric>): MutableList<AvailabilityMetric> {
        val matched = filter(items) { candidate: AvailabilityMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityMetrics(store: AvailabilityMetricStore, query: AvailabilityMetricQuery): MutableList<AvailabilityMetric> {
    return query.apply(store.list())
}
