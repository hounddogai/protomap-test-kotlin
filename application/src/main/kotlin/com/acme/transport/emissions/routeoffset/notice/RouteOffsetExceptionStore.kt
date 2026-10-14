package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset exceptions by code. */
interface RouteOffsetExceptionStore {
    fun find(code: String): RouteOffsetException?
    fun save(item: RouteOffsetException)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetExceptionStore(
    var items: MutableMap<String, RouteOffsetException>,
    var order: MutableList<String>,
) : RouteOffsetExceptionStore {
    override fun find(code: String): RouteOffsetException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetException) {
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

    override fun list(): MutableList<RouteOffsetException> {
        val out = mutableListOf<RouteOffsetException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetExceptionStore(): RouteOffsetExceptionStore {
    return MemoryRouteOffsetExceptionStore(
        mutableMapOf<String, RouteOffsetException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetExceptionStore(
    var inner: RouteOffsetExceptionStore,
    var cache: TtlCache<RouteOffsetException>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetExceptionStore {
    override fun find(code: String): RouteOffsetException? {
        val hit = (cache.get(code) as RouteOffsetException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetException> = inner.list()
}

fun newCachedRouteOffsetExceptionStore(inner: RouteOffsetExceptionStore, ttl: Int, clock: Clock): RouteOffsetExceptionStore {
    return CachedRouteOffsetExceptionStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset exceptions. Empty fields match all. */
data class RouteOffsetExceptionQuery(
    var unit: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetException): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetException>): MutableList<RouteOffsetException> {
        val matched = filter(items) { candidate: RouteOffsetException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetExceptions(store: RouteOffsetExceptionStore, query: RouteOffsetExceptionQuery): MutableList<RouteOffsetException> {
    return query.apply(store.list())
}
