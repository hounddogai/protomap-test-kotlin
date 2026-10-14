package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending capacity metrics by code. */
interface PendingCapacityMetricStore {
    fun find(code: String): PendingCapacityMetric?
    fun save(item: PendingCapacityMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCapacityMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCapacityMetricStore(
    var items: MutableMap<String, PendingCapacityMetric>,
    var order: MutableList<String>,
) : PendingCapacityMetricStore {
    override fun find(code: String): PendingCapacityMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCapacityMetric) {
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

    override fun list(): MutableList<PendingCapacityMetric> {
        val out = mutableListOf<PendingCapacityMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCapacityMetricStore(): PendingCapacityMetricStore {
    return MemoryPendingCapacityMetricStore(
        mutableMapOf<String, PendingCapacityMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCapacityMetricStore(
    var inner: PendingCapacityMetricStore,
    var cache: TtlCache<PendingCapacityMetric>,
    var hits: Int,
    var misses: Int,
) : PendingCapacityMetricStore {
    override fun find(code: String): PendingCapacityMetric? {
        val hit = (cache.get(code) as PendingCapacityMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCapacityMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCapacityMetric> = inner.list()
}

fun newCachedPendingCapacityMetricStore(inner: PendingCapacityMetricStore, ttl: Int, clock: Clock): PendingCapacityMetricStore {
    return CachedPendingCapacityMetricStore(
        inner = inner,
        cache = newTtlCache<PendingCapacityMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending capacity metrics. Empty fields match all. */
data class PendingCapacityMetricQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCapacityMetric): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingCapacityMetric>): MutableList<PendingCapacityMetric> {
        val matched = filter(items) { candidate: PendingCapacityMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCapacityMetrics(store: PendingCapacityMetricStore, query: PendingCapacityMetricQuery): MutableList<PendingCapacityMetric> {
    return query.apply(store.list())
}
