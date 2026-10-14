package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered availability reservations by code. */
interface TieredAvailabilityReservationStore {
    fun find(code: String): TieredAvailabilityReservation?
    fun save(item: TieredAvailabilityReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredAvailabilityReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredAvailabilityReservationStore(
    var items: MutableMap<String, TieredAvailabilityReservation>,
    var order: MutableList<String>,
) : TieredAvailabilityReservationStore {
    override fun find(code: String): TieredAvailabilityReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredAvailabilityReservation) {
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

    override fun list(): MutableList<TieredAvailabilityReservation> {
        val out = mutableListOf<TieredAvailabilityReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredAvailabilityReservationStore(): TieredAvailabilityReservationStore {
    return MemoryTieredAvailabilityReservationStore(
        mutableMapOf<String, TieredAvailabilityReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredAvailabilityReservationStore(
    var inner: TieredAvailabilityReservationStore,
    var cache: TtlCache<TieredAvailabilityReservation>,
    var hits: Int,
    var misses: Int,
) : TieredAvailabilityReservationStore {
    override fun find(code: String): TieredAvailabilityReservation? {
        val hit = (cache.get(code) as TieredAvailabilityReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredAvailabilityReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredAvailabilityReservation> = inner.list()
}

fun newCachedTieredAvailabilityReservationStore(inner: TieredAvailabilityReservationStore, ttl: Int, clock: Clock): TieredAvailabilityReservationStore {
    return CachedTieredAvailabilityReservationStore(
        inner = inner,
        cache = newTtlCache<TieredAvailabilityReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered availability reservations. Empty fields match all. */
data class TieredAvailabilityReservationQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredAvailabilityReservation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredAvailabilityReservation>): MutableList<TieredAvailabilityReservation> {
        val matched = filter(items) { candidate: TieredAvailabilityReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredAvailabilityReservations(store: TieredAvailabilityReservationStore, query: TieredAvailabilityReservationQuery): MutableList<TieredAvailabilityReservation> {
    return query.apply(store.list())
}
