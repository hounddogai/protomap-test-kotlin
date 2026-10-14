package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset allocations by code. */
interface RouteOffsetAllocationStore {
    fun find(code: String): RouteOffsetAllocation?
    fun save(item: RouteOffsetAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetAllocationStore(
    var items: MutableMap<String, RouteOffsetAllocation>,
    var order: MutableList<String>,
) : RouteOffsetAllocationStore {
    override fun find(code: String): RouteOffsetAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetAllocation) {
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

    override fun list(): MutableList<RouteOffsetAllocation> {
        val out = mutableListOf<RouteOffsetAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetAllocationStore(): RouteOffsetAllocationStore {
    return MemoryRouteOffsetAllocationStore(
        mutableMapOf<String, RouteOffsetAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetAllocationStore(
    var inner: RouteOffsetAllocationStore,
    var cache: TtlCache<RouteOffsetAllocation>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetAllocationStore {
    override fun find(code: String): RouteOffsetAllocation? {
        val hit = (cache.get(code) as RouteOffsetAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetAllocation> = inner.list()
}

fun newCachedRouteOffsetAllocationStore(inner: RouteOffsetAllocationStore, ttl: Int, clock: Clock): RouteOffsetAllocationStore {
    return CachedRouteOffsetAllocationStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset allocations. Empty fields match all. */
data class RouteOffsetAllocationQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetAllocation>): MutableList<RouteOffsetAllocation> {
        val matched = filter(items) { candidate: RouteOffsetAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetAllocations(store: RouteOffsetAllocationStore, query: RouteOffsetAllocationQuery): MutableList<RouteOffsetAllocation> {
    return query.apply(store.list())
}
