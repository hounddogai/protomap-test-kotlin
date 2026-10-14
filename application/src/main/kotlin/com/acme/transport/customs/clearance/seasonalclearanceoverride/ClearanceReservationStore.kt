package com.acme.transport.customs.clearance.seasonalclearanceoverride

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance reservations by code. */
interface ClearanceReservationStore {
    fun find(code: String): ClearanceReservation?
    fun save(item: ClearanceReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceReservationStore(
    var items: MutableMap<String, ClearanceReservation>,
    var order: MutableList<String>,
) : ClearanceReservationStore {
    override fun find(code: String): ClearanceReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceReservation) {
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

    override fun list(): MutableList<ClearanceReservation> {
        val out = mutableListOf<ClearanceReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceReservationStore(): ClearanceReservationStore {
    return MemoryClearanceReservationStore(
        mutableMapOf<String, ClearanceReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceReservationStore(
    var inner: ClearanceReservationStore,
    var cache: TtlCache<ClearanceReservation>,
    var hits: Int,
    var misses: Int,
) : ClearanceReservationStore {
    override fun find(code: String): ClearanceReservation? {
        val hit = (cache.get(code) as ClearanceReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceReservation> = inner.list()
}

fun newCachedClearanceReservationStore(inner: ClearanceReservationStore, ttl: Int, clock: Clock): ClearanceReservationStore {
    return CachedClearanceReservationStore(
        inner = inner,
        cache = newTtlCache<ClearanceReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance reservations. Empty fields match all. */
data class ClearanceReservationQuery(
    var segment: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceReservation): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceReservation>): MutableList<ClearanceReservation> {
        val matched = filter(items) { candidate: ClearanceReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceReservations(store: ClearanceReservationStore, query: ClearanceReservationQuery): MutableList<ClearanceReservation> {
    return query.apply(store.list())
}
