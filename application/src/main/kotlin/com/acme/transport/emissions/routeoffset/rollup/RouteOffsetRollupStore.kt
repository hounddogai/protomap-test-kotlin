package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset rollups by code. */
interface RouteOffsetRollupStore {
    fun find(code: String): RouteOffsetRollup?
    fun save(item: RouteOffsetRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetRollupStore(
    var items: MutableMap<String, RouteOffsetRollup>,
    var order: MutableList<String>,
) : RouteOffsetRollupStore {
    override fun find(code: String): RouteOffsetRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetRollup) {
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

    override fun list(): MutableList<RouteOffsetRollup> {
        val out = mutableListOf<RouteOffsetRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetRollupStore(): RouteOffsetRollupStore {
    return MemoryRouteOffsetRollupStore(mutableMapOf<String, RouteOffsetRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetRollupStore(
    var inner: RouteOffsetRollupStore,
    var cache: TtlCache<RouteOffsetRollup>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetRollupStore {
    override fun find(code: String): RouteOffsetRollup? {
        val hit = (cache.get(code) as RouteOffsetRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetRollup> = inner.list()
}

fun newCachedRouteOffsetRollupStore(inner: RouteOffsetRollupStore, ttl: Int, clock: Clock): RouteOffsetRollupStore {
    return CachedRouteOffsetRollupStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset rollups. Empty fields match all. */
data class RouteOffsetRollupQuery(
    var segment: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetRollup): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<RouteOffsetRollup>): MutableList<RouteOffsetRollup> {
        val matched = filter(items) { candidate: RouteOffsetRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetRollups(store: RouteOffsetRollupStore, query: RouteOffsetRollupQuery): MutableList<RouteOffsetRollup> {
    return query.apply(store.list())
}
