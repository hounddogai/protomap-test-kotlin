package com.acme.transport.emissions.routeoffset.metric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset claims by code. */
interface RouteOffsetClaimStore {
    fun find(code: String): RouteOffsetClaim?
    fun save(item: RouteOffsetClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetClaimStore(
    var items: MutableMap<String, RouteOffsetClaim>,
    var order: MutableList<String>,
) : RouteOffsetClaimStore {
    override fun find(code: String): RouteOffsetClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetClaim) {
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

    override fun list(): MutableList<RouteOffsetClaim> {
        val out = mutableListOf<RouteOffsetClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetClaimStore(): RouteOffsetClaimStore {
    return MemoryRouteOffsetClaimStore(mutableMapOf<String, RouteOffsetClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetClaimStore(
    var inner: RouteOffsetClaimStore,
    var cache: TtlCache<RouteOffsetClaim>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetClaimStore {
    override fun find(code: String): RouteOffsetClaim? {
        val hit = (cache.get(code) as RouteOffsetClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetClaim> = inner.list()
}

fun newCachedRouteOffsetClaimStore(inner: RouteOffsetClaimStore, ttl: Int, clock: Clock): RouteOffsetClaimStore {
    return CachedRouteOffsetClaimStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset claims. Empty fields match all. */
data class RouteOffsetClaimQuery(
    var category: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetClaim): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetClaim>): MutableList<RouteOffsetClaim> {
        val matched = filter(items) { candidate: RouteOffsetClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetClaims(store: RouteOffsetClaimStore, query: RouteOffsetClaimQuery): MutableList<RouteOffsetClaim> {
    return query.apply(store.list())
}
