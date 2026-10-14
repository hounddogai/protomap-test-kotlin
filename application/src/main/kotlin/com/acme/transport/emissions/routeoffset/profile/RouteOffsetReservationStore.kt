package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset reservations by code. */
interface RouteOffsetReservationStore {
    fun find(code: String): RouteOffsetReservation?
    fun save(item: RouteOffsetReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetReservationStore(
    var items: MutableMap<String, RouteOffsetReservation>,
    var order: MutableList<String>,
) : RouteOffsetReservationStore {
    override fun find(code: String): RouteOffsetReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetReservation) {
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

    override fun list(): MutableList<RouteOffsetReservation> {
        val out = mutableListOf<RouteOffsetReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetReservationStore(): RouteOffsetReservationStore {
    return MemoryRouteOffsetReservationStore(
        mutableMapOf<String, RouteOffsetReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetReservationStore(
    var inner: RouteOffsetReservationStore,
    var cache: TtlCache<RouteOffsetReservation>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetReservationStore {
    override fun find(code: String): RouteOffsetReservation? {
        val hit = (cache.get(code) as RouteOffsetReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetReservation> = inner.list()
}

fun newCachedRouteOffsetReservationStore(inner: RouteOffsetReservationStore, ttl: Int, clock: Clock): RouteOffsetReservationStore {
    return CachedRouteOffsetReservationStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset reservations. Empty fields match all. */
data class RouteOffsetReservationQuery(
    var source: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetReservation): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetReservation>): MutableList<RouteOffsetReservation> {
        val matched = filter(items) { candidate: RouteOffsetReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetReservations(store: RouteOffsetReservationStore, query: RouteOffsetReservationQuery): MutableList<RouteOffsetReservation> {
    return query.apply(store.list())
}
