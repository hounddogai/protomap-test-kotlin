package com.acme.transport.emissions.routeoffset.metric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset metrics by code. */
interface RouteOffsetMetricStore {
    fun find(code: String): RouteOffsetMetric?
    fun save(item: RouteOffsetMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetMetricStore(
    var items: MutableMap<String, RouteOffsetMetric>,
    var order: MutableList<String>,
) : RouteOffsetMetricStore {
    override fun find(code: String): RouteOffsetMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetMetric) {
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

    override fun list(): MutableList<RouteOffsetMetric> {
        val out = mutableListOf<RouteOffsetMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetMetricStore(): RouteOffsetMetricStore {
    return MemoryRouteOffsetMetricStore(mutableMapOf<String, RouteOffsetMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetMetricStore(
    var inner: RouteOffsetMetricStore,
    var cache: TtlCache<RouteOffsetMetric>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetMetricStore {
    override fun find(code: String): RouteOffsetMetric? {
        val hit = (cache.get(code) as RouteOffsetMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetMetric> = inner.list()
}

fun newCachedRouteOffsetMetricStore(inner: RouteOffsetMetricStore, ttl: Int, clock: Clock): RouteOffsetMetricStore {
    return CachedRouteOffsetMetricStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset metrics. Empty fields match all. */
data class RouteOffsetMetricQuery(
    var lane: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetMetric): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetMetric>): MutableList<RouteOffsetMetric> {
        val matched = filter(items) { candidate: RouteOffsetMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetMetrics(store: RouteOffsetMetricStore, query: RouteOffsetMetricQuery): MutableList<RouteOffsetMetric> {
    return query.apply(store.list())
}
