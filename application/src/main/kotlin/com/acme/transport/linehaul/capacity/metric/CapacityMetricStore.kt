package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity metrics by code. */
interface CapacityMetricStore {
    fun find(code: String): CapacityMetric?
    fun save(item: CapacityMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityMetricStore(
    var items: MutableMap<String, CapacityMetric>,
    var order: MutableList<String>,
) : CapacityMetricStore {
    override fun find(code: String): CapacityMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityMetric) {
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

    override fun list(): MutableList<CapacityMetric> {
        val out = mutableListOf<CapacityMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityMetricStore(): CapacityMetricStore {
    return MemoryCapacityMetricStore(mutableMapOf<String, CapacityMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityMetricStore(
    var inner: CapacityMetricStore,
    var cache: TtlCache<CapacityMetric>,
    var hits: Int,
    var misses: Int,
) : CapacityMetricStore {
    override fun find(code: String): CapacityMetric? {
        val hit = (cache.get(code) as CapacityMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityMetric> = inner.list()
}

fun newCachedCapacityMetricStore(inner: CapacityMetricStore, ttl: Int, clock: Clock): CapacityMetricStore {
    return CachedCapacityMetricStore(
        inner = inner,
        cache = newTtlCache<CapacityMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity metrics. Empty fields match all. */
data class CapacityMetricQuery(
    var lane: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityMetric): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<CapacityMetric>): MutableList<CapacityMetric> {
        val matched = filter(items) { candidate: CapacityMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityMetrics(store: CapacityMetricStore, query: CapacityMetricQuery): MutableList<CapacityMetric> {
    return query.apply(store.list())
}
