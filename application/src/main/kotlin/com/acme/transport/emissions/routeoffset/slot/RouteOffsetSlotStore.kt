package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset slots by code. */
interface RouteOffsetSlotStore {
    fun find(code: String): RouteOffsetSlot?
    fun save(item: RouteOffsetSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetSlotStore(
    var items: MutableMap<String, RouteOffsetSlot>,
    var order: MutableList<String>,
) : RouteOffsetSlotStore {
    override fun find(code: String): RouteOffsetSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetSlot) {
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

    override fun list(): MutableList<RouteOffsetSlot> {
        val out = mutableListOf<RouteOffsetSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetSlotStore(): RouteOffsetSlotStore {
    return MemoryRouteOffsetSlotStore(mutableMapOf<String, RouteOffsetSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetSlotStore(
    var inner: RouteOffsetSlotStore,
    var cache: TtlCache<RouteOffsetSlot>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetSlotStore {
    override fun find(code: String): RouteOffsetSlot? {
        val hit = (cache.get(code) as RouteOffsetSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetSlot> = inner.list()
}

fun newCachedRouteOffsetSlotStore(inner: RouteOffsetSlotStore, ttl: Int, clock: Clock): RouteOffsetSlotStore {
    return CachedRouteOffsetSlotStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset slots. Empty fields match all. */
data class RouteOffsetSlotQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RouteOffsetSlot>): MutableList<RouteOffsetSlot> {
        val matched = filter(items) { candidate: RouteOffsetSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetSlots(store: RouteOffsetSlotStore, query: RouteOffsetSlotQuery): MutableList<RouteOffsetSlot> {
    return query.apply(store.list())
}
