package com.acme.transport.fleetops.tire.claim

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire metrics by code. */
interface TireMetricStore {
    fun find(code: String): TireMetric?
    fun save(item: TireMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireMetricStore(
    var items: MutableMap<String, TireMetric>,
    var order: MutableList<String>,
) : TireMetricStore {
    override fun find(code: String): TireMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireMetric) {
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

    override fun list(): MutableList<TireMetric> {
        val out = mutableListOf<TireMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireMetricStore(): TireMetricStore {
    return MemoryTireMetricStore(mutableMapOf<String, TireMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireMetricStore(
    var inner: TireMetricStore,
    var cache: TtlCache<TireMetric>,
    var hits: Int,
    var misses: Int,
) : TireMetricStore {
    override fun find(code: String): TireMetric? {
        val hit = (cache.get(code) as TireMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireMetric> = inner.list()
}

fun newCachedTireMetricStore(inner: TireMetricStore, ttl: Int, clock: Clock): TireMetricStore {
    return CachedTireMetricStore(
        inner = inner,
        cache = newTtlCache<TireMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire metrics. Empty fields match all. */
data class TireMetricQuery(
    var locale: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireMetric): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireMetric>): MutableList<TireMetric> {
        val matched = filter(items) { candidate: TireMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireMetrics(store: TireMetricStore, query: TireMetricQuery): MutableList<TireMetric> {
    return query.apply(store.list())
}
