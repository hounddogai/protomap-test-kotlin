package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset cycles by code. */
interface RouteOffsetCycleStore {
    fun find(code: String): RouteOffsetCycle?
    fun save(item: RouteOffsetCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetCycleStore(
    var items: MutableMap<String, RouteOffsetCycle>,
    var order: MutableList<String>,
) : RouteOffsetCycleStore {
    override fun find(code: String): RouteOffsetCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetCycle) {
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

    override fun list(): MutableList<RouteOffsetCycle> {
        val out = mutableListOf<RouteOffsetCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetCycleStore(): RouteOffsetCycleStore {
    return MemoryRouteOffsetCycleStore(mutableMapOf<String, RouteOffsetCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetCycleStore(
    var inner: RouteOffsetCycleStore,
    var cache: TtlCache<RouteOffsetCycle>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetCycleStore {
    override fun find(code: String): RouteOffsetCycle? {
        val hit = (cache.get(code) as RouteOffsetCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetCycle> = inner.list()
}

fun newCachedRouteOffsetCycleStore(inner: RouteOffsetCycleStore, ttl: Int, clock: Clock): RouteOffsetCycleStore {
    return CachedRouteOffsetCycleStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset cycles. Empty fields match all. */
data class RouteOffsetCycleQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetCycle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetCycle>): MutableList<RouteOffsetCycle> {
        val matched = filter(items) { candidate: RouteOffsetCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetCycles(store: RouteOffsetCycleStore, query: RouteOffsetCycleQuery): MutableList<RouteOffsetCycle> {
    return query.apply(store.list())
}
