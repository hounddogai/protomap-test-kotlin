package com.acme.transport.linehaul.trailer.traileroverride

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer reservations by code. */
interface TrailerReservationStore {
    fun find(code: String): TrailerReservation?
    fun save(item: TrailerReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerReservationStore(
    var items: MutableMap<String, TrailerReservation>,
    var order: MutableList<String>,
) : TrailerReservationStore {
    override fun find(code: String): TrailerReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerReservation) {
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

    override fun list(): MutableList<TrailerReservation> {
        val out = mutableListOf<TrailerReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerReservationStore(): TrailerReservationStore {
    return MemoryTrailerReservationStore(mutableMapOf<String, TrailerReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerReservationStore(
    var inner: TrailerReservationStore,
    var cache: TtlCache<TrailerReservation>,
    var hits: Int,
    var misses: Int,
) : TrailerReservationStore {
    override fun find(code: String): TrailerReservation? {
        val hit = (cache.get(code) as TrailerReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerReservation> = inner.list()
}

fun newCachedTrailerReservationStore(inner: TrailerReservationStore, ttl: Int, clock: Clock): TrailerReservationStore {
    return CachedTrailerReservationStore(
        inner = inner,
        cache = newTtlCache<TrailerReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer reservations. Empty fields match all. */
data class TrailerReservationQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerReservation>): MutableList<TrailerReservation> {
        val matched = filter(items) { candidate: TrailerReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerReservations(store: TrailerReservationStore, query: TrailerReservationQuery): MutableList<TrailerReservation> {
    return query.apply(store.list())
}
