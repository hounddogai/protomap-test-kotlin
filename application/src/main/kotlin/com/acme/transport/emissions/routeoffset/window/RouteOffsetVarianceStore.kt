package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset variances by code. */
interface RouteOffsetVarianceStore {
    fun find(code: String): RouteOffsetVariance?
    fun save(item: RouteOffsetVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetVarianceStore(
    var items: MutableMap<String, RouteOffsetVariance>,
    var order: MutableList<String>,
) : RouteOffsetVarianceStore {
    override fun find(code: String): RouteOffsetVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetVariance) {
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

    override fun list(): MutableList<RouteOffsetVariance> {
        val out = mutableListOf<RouteOffsetVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetVarianceStore(): RouteOffsetVarianceStore {
    return MemoryRouteOffsetVarianceStore(mutableMapOf<String, RouteOffsetVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetVarianceStore(
    var inner: RouteOffsetVarianceStore,
    var cache: TtlCache<RouteOffsetVariance>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetVarianceStore {
    override fun find(code: String): RouteOffsetVariance? {
        val hit = (cache.get(code) as RouteOffsetVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetVariance> = inner.list()
}

fun newCachedRouteOffsetVarianceStore(inner: RouteOffsetVarianceStore, ttl: Int, clock: Clock): RouteOffsetVarianceStore {
    return CachedRouteOffsetVarianceStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset variances. Empty fields match all. */
data class RouteOffsetVarianceQuery(
    var country: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetVariance): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetVariance>): MutableList<RouteOffsetVariance> {
        val matched = filter(items) { candidate: RouteOffsetVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetVariances(store: RouteOffsetVarianceStore, query: RouteOffsetVarianceQuery): MutableList<RouteOffsetVariance> {
    return query.apply(store.list())
}
