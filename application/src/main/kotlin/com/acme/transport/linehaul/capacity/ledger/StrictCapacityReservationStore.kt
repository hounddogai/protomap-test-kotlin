package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict capacity reservations by code. */
interface StrictCapacityReservationStore {
    fun find(code: String): StrictCapacityReservation?
    fun save(item: StrictCapacityReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCapacityReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCapacityReservationStore(
    var items: MutableMap<String, StrictCapacityReservation>,
    var order: MutableList<String>,
) : StrictCapacityReservationStore {
    override fun find(code: String): StrictCapacityReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCapacityReservation) {
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

    override fun list(): MutableList<StrictCapacityReservation> {
        val out = mutableListOf<StrictCapacityReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCapacityReservationStore(): StrictCapacityReservationStore {
    return MemoryStrictCapacityReservationStore(
        mutableMapOf<String, StrictCapacityReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCapacityReservationStore(
    var inner: StrictCapacityReservationStore,
    var cache: TtlCache<StrictCapacityReservation>,
    var hits: Int,
    var misses: Int,
) : StrictCapacityReservationStore {
    override fun find(code: String): StrictCapacityReservation? {
        val hit = (cache.get(code) as StrictCapacityReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCapacityReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCapacityReservation> = inner.list()
}

fun newCachedStrictCapacityReservationStore(inner: StrictCapacityReservationStore, ttl: Int, clock: Clock): StrictCapacityReservationStore {
    return CachedStrictCapacityReservationStore(
        inner = inner,
        cache = newTtlCache<StrictCapacityReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict capacity reservations. Empty fields match all. */
data class StrictCapacityReservationQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCapacityReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictCapacityReservation>): MutableList<StrictCapacityReservation> {
        val matched = filter(items) { candidate: StrictCapacityReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCapacityReservations(store: StrictCapacityReservationStore, query: StrictCapacityReservationQuery): MutableList<StrictCapacityReservation> {
    return query.apply(store.list())
}
