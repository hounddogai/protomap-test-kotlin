package com.acme.transport.lastmile.locker.lockerrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered locker metrics by code. */
interface TieredLockerMetricStore {
    fun find(code: String): TieredLockerMetric?
    fun save(item: TieredLockerMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredLockerMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredLockerMetricStore(
    var items: MutableMap<String, TieredLockerMetric>,
    var order: MutableList<String>,
) : TieredLockerMetricStore {
    override fun find(code: String): TieredLockerMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredLockerMetric) {
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

    override fun list(): MutableList<TieredLockerMetric> {
        val out = mutableListOf<TieredLockerMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredLockerMetricStore(): TieredLockerMetricStore {
    return MemoryTieredLockerMetricStore(mutableMapOf<String, TieredLockerMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredLockerMetricStore(
    var inner: TieredLockerMetricStore,
    var cache: TtlCache<TieredLockerMetric>,
    var hits: Int,
    var misses: Int,
) : TieredLockerMetricStore {
    override fun find(code: String): TieredLockerMetric? {
        val hit = (cache.get(code) as TieredLockerMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredLockerMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredLockerMetric> = inner.list()
}

fun newCachedTieredLockerMetricStore(inner: TieredLockerMetricStore, ttl: Int, clock: Clock): TieredLockerMetricStore {
    return CachedTieredLockerMetricStore(
        inner = inner,
        cache = newTtlCache<TieredLockerMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered locker metrics. Empty fields match all. */
data class TieredLockerMetricQuery(
    var depot: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredLockerMetric): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredLockerMetric>): MutableList<TieredLockerMetric> {
        val matched = filter(items) { candidate: TieredLockerMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredLockerMetrics(store: TieredLockerMetricStore, query: TieredLockerMetricQuery): MutableList<TieredLockerMetric> {
    return query.apply(store.list())
}
