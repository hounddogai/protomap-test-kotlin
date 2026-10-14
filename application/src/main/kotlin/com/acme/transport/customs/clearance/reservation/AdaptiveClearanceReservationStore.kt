package com.acme.transport.customs.clearance.reservation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive clearance reservations by code. */
interface AdaptiveClearanceReservationStore {
    fun find(code: String): AdaptiveClearanceReservation?
    fun save(item: AdaptiveClearanceReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveClearanceReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveClearanceReservationStore(
    var items: MutableMap<String, AdaptiveClearanceReservation>,
    var order: MutableList<String>,
) : AdaptiveClearanceReservationStore {
    override fun find(code: String): AdaptiveClearanceReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveClearanceReservation) {
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

    override fun list(): MutableList<AdaptiveClearanceReservation> {
        val out = mutableListOf<AdaptiveClearanceReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveClearanceReservationStore(): AdaptiveClearanceReservationStore {
    return MemoryAdaptiveClearanceReservationStore(
        mutableMapOf<String, AdaptiveClearanceReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveClearanceReservationStore(
    var inner: AdaptiveClearanceReservationStore,
    var cache: TtlCache<AdaptiveClearanceReservation>,
    var hits: Int,
    var misses: Int,
) : AdaptiveClearanceReservationStore {
    override fun find(code: String): AdaptiveClearanceReservation? {
        val hit = (cache.get(code) as AdaptiveClearanceReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveClearanceReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveClearanceReservation> = inner.list()
}

fun newCachedAdaptiveClearanceReservationStore(inner: AdaptiveClearanceReservationStore, ttl: Int, clock: Clock): AdaptiveClearanceReservationStore {
    return CachedAdaptiveClearanceReservationStore(
        inner = inner,
        cache = newTtlCache<AdaptiveClearanceReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive clearance reservations. Empty fields match all. */
data class AdaptiveClearanceReservationQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveClearanceReservation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveClearanceReservation>): MutableList<AdaptiveClearanceReservation> {
        val matched = filter(items) { candidate: AdaptiveClearanceReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveClearanceReservations(store: AdaptiveClearanceReservationStore, query: AdaptiveClearanceReservationQuery): MutableList<AdaptiveClearanceReservation> {
    return query.apply(store.list())
}
