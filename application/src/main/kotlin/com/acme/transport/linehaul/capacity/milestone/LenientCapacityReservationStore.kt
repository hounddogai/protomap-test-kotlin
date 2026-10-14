package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient capacity reservations by code. */
interface LenientCapacityReservationStore {
    fun find(code: String): LenientCapacityReservation?
    fun save(item: LenientCapacityReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCapacityReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCapacityReservationStore(
    var items: MutableMap<String, LenientCapacityReservation>,
    var order: MutableList<String>,
) : LenientCapacityReservationStore {
    override fun find(code: String): LenientCapacityReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCapacityReservation) {
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

    override fun list(): MutableList<LenientCapacityReservation> {
        val out = mutableListOf<LenientCapacityReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCapacityReservationStore(): LenientCapacityReservationStore {
    return MemoryLenientCapacityReservationStore(
        mutableMapOf<String, LenientCapacityReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCapacityReservationStore(
    var inner: LenientCapacityReservationStore,
    var cache: TtlCache<LenientCapacityReservation>,
    var hits: Int,
    var misses: Int,
) : LenientCapacityReservationStore {
    override fun find(code: String): LenientCapacityReservation? {
        val hit = (cache.get(code) as LenientCapacityReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCapacityReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCapacityReservation> = inner.list()
}

fun newCachedLenientCapacityReservationStore(inner: LenientCapacityReservationStore, ttl: Int, clock: Clock): LenientCapacityReservationStore {
    return CachedLenientCapacityReservationStore(
        inner = inner,
        cache = newTtlCache<LenientCapacityReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient capacity reservations. Empty fields match all. */
data class LenientCapacityReservationQuery(
    var depot: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCapacityReservation): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCapacityReservation>): MutableList<LenientCapacityReservation> {
        val matched = filter(items) { candidate: LenientCapacityReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCapacityReservations(store: LenientCapacityReservationStore, query: LenientCapacityReservationQuery): MutableList<LenientCapacityReservation> {
    return query.apply(store.list())
}
