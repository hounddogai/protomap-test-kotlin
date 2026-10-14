package com.acme.transport.network.zone.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone metrics by code. */
interface ZoneMetricStore {
    fun find(code: String): ZoneMetric?
    fun save(item: ZoneMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneMetricStore(
    var items: MutableMap<String, ZoneMetric>,
    var order: MutableList<String>,
) : ZoneMetricStore {
    override fun find(code: String): ZoneMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneMetric) {
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

    override fun list(): MutableList<ZoneMetric> {
        val out = mutableListOf<ZoneMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneMetricStore(): ZoneMetricStore {
    return MemoryZoneMetricStore(mutableMapOf<String, ZoneMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneMetricStore(
    var inner: ZoneMetricStore,
    var cache: TtlCache<ZoneMetric>,
    var hits: Int,
    var misses: Int,
) : ZoneMetricStore {
    override fun find(code: String): ZoneMetric? {
        val hit = (cache.get(code) as ZoneMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneMetric> = inner.list()
}

fun newCachedZoneMetricStore(inner: ZoneMetricStore, ttl: Int, clock: Clock): ZoneMetricStore {
    return CachedZoneMetricStore(
        inner = inner,
        cache = newTtlCache<ZoneMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone metrics. Empty fields match all. */
data class ZoneMetricQuery(
    var segment: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneMetric): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneMetric>): MutableList<ZoneMetric> {
        val matched = filter(items) { candidate: ZoneMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneMetrics(store: ZoneMetricStore, query: ZoneMetricQuery): MutableList<ZoneMetric> {
    return query.apply(store.list())
}
