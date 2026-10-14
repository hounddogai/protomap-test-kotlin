package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending tire metrics by code. */
interface PendingTireMetricStore {
    fun find(code: String): PendingTireMetric?
    fun save(item: PendingTireMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTireMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTireMetricStore(
    var items: MutableMap<String, PendingTireMetric>,
    var order: MutableList<String>,
) : PendingTireMetricStore {
    override fun find(code: String): PendingTireMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTireMetric) {
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

    override fun list(): MutableList<PendingTireMetric> {
        val out = mutableListOf<PendingTireMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTireMetricStore(): PendingTireMetricStore {
    return MemoryPendingTireMetricStore(mutableMapOf<String, PendingTireMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTireMetricStore(
    var inner: PendingTireMetricStore,
    var cache: TtlCache<PendingTireMetric>,
    var hits: Int,
    var misses: Int,
) : PendingTireMetricStore {
    override fun find(code: String): PendingTireMetric? {
        val hit = (cache.get(code) as PendingTireMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTireMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTireMetric> = inner.list()
}

fun newCachedPendingTireMetricStore(inner: PendingTireMetricStore, ttl: Int, clock: Clock): PendingTireMetricStore {
    return CachedPendingTireMetricStore(
        inner = inner,
        cache = newTtlCache<PendingTireMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending tire metrics. Empty fields match all. */
data class PendingTireMetricQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTireMetric): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingTireMetric>): MutableList<PendingTireMetric> {
        val matched = filter(items) { candidate: PendingTireMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTireMetrics(store: PendingTireMetricStore, query: PendingTireMetricQuery): MutableList<PendingTireMetric> {
    return query.apply(store.list())
}
