package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset profiles by code. */
interface RouteOffsetProfileStore {
    fun find(code: String): RouteOffsetProfile?
    fun save(item: RouteOffsetProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetProfileStore(
    var items: MutableMap<String, RouteOffsetProfile>,
    var order: MutableList<String>,
) : RouteOffsetProfileStore {
    override fun find(code: String): RouteOffsetProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetProfile) {
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

    override fun list(): MutableList<RouteOffsetProfile> {
        val out = mutableListOf<RouteOffsetProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetProfileStore(): RouteOffsetProfileStore {
    return MemoryRouteOffsetProfileStore(mutableMapOf<String, RouteOffsetProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetProfileStore(
    var inner: RouteOffsetProfileStore,
    var cache: TtlCache<RouteOffsetProfile>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetProfileStore {
    override fun find(code: String): RouteOffsetProfile? {
        val hit = (cache.get(code) as RouteOffsetProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetProfile> = inner.list()
}

fun newCachedRouteOffsetProfileStore(inner: RouteOffsetProfileStore, ttl: Int, clock: Clock): RouteOffsetProfileStore {
    return CachedRouteOffsetProfileStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset profiles. Empty fields match all. */
data class RouteOffsetProfileQuery(
    var depot: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetProfile): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetProfile>): MutableList<RouteOffsetProfile> {
        val matched = filter(items) { candidate: RouteOffsetProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetProfiles(store: RouteOffsetProfileStore, query: RouteOffsetProfileQuery): MutableList<RouteOffsetProfile> {
    return query.apply(store.list())
}
