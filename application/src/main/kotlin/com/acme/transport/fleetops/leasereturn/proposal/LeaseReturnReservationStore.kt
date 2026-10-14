package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return reservations by code. */
interface LeaseReturnReservationStore {
    fun find(code: String): LeaseReturnReservation?
    fun save(item: LeaseReturnReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnReservationStore(
    var items: MutableMap<String, LeaseReturnReservation>,
    var order: MutableList<String>,
) : LeaseReturnReservationStore {
    override fun find(code: String): LeaseReturnReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnReservation) {
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

    override fun list(): MutableList<LeaseReturnReservation> {
        val out = mutableListOf<LeaseReturnReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnReservationStore(): LeaseReturnReservationStore {
    return MemoryLeaseReturnReservationStore(
        mutableMapOf<String, LeaseReturnReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnReservationStore(
    var inner: LeaseReturnReservationStore,
    var cache: TtlCache<LeaseReturnReservation>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnReservationStore {
    override fun find(code: String): LeaseReturnReservation? {
        val hit = (cache.get(code) as LeaseReturnReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnReservation> = inner.list()
}

fun newCachedLeaseReturnReservationStore(inner: LeaseReturnReservationStore, ttl: Int, clock: Clock): LeaseReturnReservationStore {
    return CachedLeaseReturnReservationStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return reservations. Empty fields match all. */
data class LeaseReturnReservationQuery(
    var depot: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnReservation): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnReservation>): MutableList<LeaseReturnReservation> {
        val matched = filter(items) { candidate: LeaseReturnReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnReservations(store: LeaseReturnReservationStore, query: LeaseReturnReservationQuery): MutableList<LeaseReturnReservation> {
    return query.apply(store.list())
}
