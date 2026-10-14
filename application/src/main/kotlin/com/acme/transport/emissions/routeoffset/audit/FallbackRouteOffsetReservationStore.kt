package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback route offset reservations by code. */
interface FallbackRouteOffsetReservationStore {
    fun find(code: String): FallbackRouteOffsetReservation?
    fun save(item: FallbackRouteOffsetReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRouteOffsetReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRouteOffsetReservationStore(
    var items: MutableMap<String, FallbackRouteOffsetReservation>,
    var order: MutableList<String>,
) : FallbackRouteOffsetReservationStore {
    override fun find(code: String): FallbackRouteOffsetReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRouteOffsetReservation) {
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

    override fun list(): MutableList<FallbackRouteOffsetReservation> {
        val out = mutableListOf<FallbackRouteOffsetReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRouteOffsetReservationStore(): FallbackRouteOffsetReservationStore {
    return MemoryFallbackRouteOffsetReservationStore(
        mutableMapOf<String, FallbackRouteOffsetReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRouteOffsetReservationStore(
    var inner: FallbackRouteOffsetReservationStore,
    var cache: TtlCache<FallbackRouteOffsetReservation>,
    var hits: Int,
    var misses: Int,
) : FallbackRouteOffsetReservationStore {
    override fun find(code: String): FallbackRouteOffsetReservation? {
        val hit = (cache.get(code) as FallbackRouteOffsetReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRouteOffsetReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRouteOffsetReservation> = inner.list()
}

fun newCachedFallbackRouteOffsetReservationStore(inner: FallbackRouteOffsetReservationStore, ttl: Int, clock: Clock): FallbackRouteOffsetReservationStore {
    return CachedFallbackRouteOffsetReservationStore(
        inner = inner,
        cache = newTtlCache<FallbackRouteOffsetReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback route offset reservations. Empty fields match all. */
data class FallbackRouteOffsetReservationQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRouteOffsetReservation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRouteOffsetReservation>): MutableList<FallbackRouteOffsetReservation> {
        val matched = filter(items) { candidate: FallbackRouteOffsetReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRouteOffsetReservations(store: FallbackRouteOffsetReservationStore, query: FallbackRouteOffsetReservationQuery): MutableList<FallbackRouteOffsetReservation> {
    return query.apply(store.list())
}
