package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization reservations by code. */
interface UtilizationReservationStore {
    fun find(code: String): UtilizationReservation?
    fun save(item: UtilizationReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationReservationStore(
    var items: MutableMap<String, UtilizationReservation>,
    var order: MutableList<String>,
) : UtilizationReservationStore {
    override fun find(code: String): UtilizationReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationReservation) {
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

    override fun list(): MutableList<UtilizationReservation> {
        val out = mutableListOf<UtilizationReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationReservationStore(): UtilizationReservationStore {
    return MemoryUtilizationReservationStore(
        mutableMapOf<String, UtilizationReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationReservationStore(
    var inner: UtilizationReservationStore,
    var cache: TtlCache<UtilizationReservation>,
    var hits: Int,
    var misses: Int,
) : UtilizationReservationStore {
    override fun find(code: String): UtilizationReservation? {
        val hit = (cache.get(code) as UtilizationReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationReservation> = inner.list()
}

fun newCachedUtilizationReservationStore(inner: UtilizationReservationStore, ttl: Int, clock: Clock): UtilizationReservationStore {
    return CachedUtilizationReservationStore(
        inner = inner,
        cache = newTtlCache<UtilizationReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization reservations. Empty fields match all. */
data class UtilizationReservationQuery(
    var depot: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationReservation): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationReservation>): MutableList<UtilizationReservation> {
        val matched = filter(items) { candidate: UtilizationReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationReservations(store: UtilizationReservationStore, query: UtilizationReservationQuery): MutableList<UtilizationReservation> {
    return query.apply(store.list())
}
