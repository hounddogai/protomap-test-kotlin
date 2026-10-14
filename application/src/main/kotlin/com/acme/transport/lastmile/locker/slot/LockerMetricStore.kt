package com.acme.transport.lastmile.locker.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker metrics by code. */
interface LockerMetricStore {
    fun find(code: String): LockerMetric?
    fun save(item: LockerMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerMetricStore(
    var items: MutableMap<String, LockerMetric>,
    var order: MutableList<String>,
) : LockerMetricStore {
    override fun find(code: String): LockerMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerMetric) {
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

    override fun list(): MutableList<LockerMetric> {
        val out = mutableListOf<LockerMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerMetricStore(): LockerMetricStore {
    return MemoryLockerMetricStore(mutableMapOf<String, LockerMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerMetricStore(
    var inner: LockerMetricStore,
    var cache: TtlCache<LockerMetric>,
    var hits: Int,
    var misses: Int,
) : LockerMetricStore {
    override fun find(code: String): LockerMetric? {
        val hit = (cache.get(code) as LockerMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerMetric> = inner.list()
}

fun newCachedLockerMetricStore(inner: LockerMetricStore, ttl: Int, clock: Clock): LockerMetricStore {
    return CachedLockerMetricStore(
        inner = inner,
        cache = newTtlCache<LockerMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker metrics. Empty fields match all. */
data class LockerMetricQuery(
    var depot: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerMetric): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<LockerMetric>): MutableList<LockerMetric> {
        val matched = filter(items) { candidate: LockerMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerMetrics(store: LockerMetricStore, query: LockerMetricQuery): MutableList<LockerMetric> {
    return query.apply(store.list())
}
