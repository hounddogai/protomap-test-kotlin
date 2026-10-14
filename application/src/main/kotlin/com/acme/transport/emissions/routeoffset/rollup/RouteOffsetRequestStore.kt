package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset requests by code. */
interface RouteOffsetRequestStore {
    fun find(code: String): RouteOffsetRequest?
    fun save(item: RouteOffsetRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetRequestStore(
    var items: MutableMap<String, RouteOffsetRequest>,
    var order: MutableList<String>,
) : RouteOffsetRequestStore {
    override fun find(code: String): RouteOffsetRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetRequest) {
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

    override fun list(): MutableList<RouteOffsetRequest> {
        val out = mutableListOf<RouteOffsetRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetRequestStore(): RouteOffsetRequestStore {
    return MemoryRouteOffsetRequestStore(mutableMapOf<String, RouteOffsetRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetRequestStore(
    var inner: RouteOffsetRequestStore,
    var cache: TtlCache<RouteOffsetRequest>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetRequestStore {
    override fun find(code: String): RouteOffsetRequest? {
        val hit = (cache.get(code) as RouteOffsetRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetRequest> = inner.list()
}

fun newCachedRouteOffsetRequestStore(inner: RouteOffsetRequestStore, ttl: Int, clock: Clock): RouteOffsetRequestStore {
    return CachedRouteOffsetRequestStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset requests. Empty fields match all. */
data class RouteOffsetRequestQuery(
    var channel: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<RouteOffsetRequest>): MutableList<RouteOffsetRequest> {
        val matched = filter(items) { candidate: RouteOffsetRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetRequests(store: RouteOffsetRequestStore, query: RouteOffsetRequestQuery): MutableList<RouteOffsetRequest> {
    return query.apply(store.list())
}
