package com.acme.transport.crewing.availability.reservation

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability reservations by code. */
interface AvailabilityReservationStore {
    fun find(code: String): AvailabilityReservation?
    fun save(item: AvailabilityReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityReservationStore(
    var items: MutableMap<String, AvailabilityReservation>,
    var order: MutableList<String>,
) : AvailabilityReservationStore {
    override fun find(code: String): AvailabilityReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityReservation) {
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

    override fun list(): MutableList<AvailabilityReservation> {
        val out = mutableListOf<AvailabilityReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityReservationStore(): AvailabilityReservationStore {
    return MemoryAvailabilityReservationStore(
        mutableMapOf<String, AvailabilityReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityReservationStore(
    var inner: AvailabilityReservationStore,
    var cache: TtlCache<AvailabilityReservation>,
    var hits: Int,
    var misses: Int,
) : AvailabilityReservationStore {
    override fun find(code: String): AvailabilityReservation? {
        val hit = (cache.get(code) as AvailabilityReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityReservation> = inner.list()
}

fun newCachedAvailabilityReservationStore(inner: AvailabilityReservationStore, ttl: Int, clock: Clock): AvailabilityReservationStore {
    return CachedAvailabilityReservationStore(
        inner = inner,
        cache = newTtlCache<AvailabilityReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability reservations. Empty fields match all. */
data class AvailabilityReservationQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityReservation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityReservation>): MutableList<AvailabilityReservation> {
        val matched = filter(items) { candidate: AvailabilityReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityReservations(store: AvailabilityReservationStore, query: AvailabilityReservationQuery): MutableList<AvailabilityReservation> {
    return query.apply(store.list())
}
