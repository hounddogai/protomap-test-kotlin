package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional cross dock reservations by code. */
interface RegionalCrossDockReservationStore {
    fun find(code: String): RegionalCrossDockReservation?
    fun save(item: RegionalCrossDockReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCrossDockReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCrossDockReservationStore(
    var items: MutableMap<String, RegionalCrossDockReservation>,
    var order: MutableList<String>,
) : RegionalCrossDockReservationStore {
    override fun find(code: String): RegionalCrossDockReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCrossDockReservation) {
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

    override fun list(): MutableList<RegionalCrossDockReservation> {
        val out = mutableListOf<RegionalCrossDockReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCrossDockReservationStore(): RegionalCrossDockReservationStore {
    return MemoryRegionalCrossDockReservationStore(
        mutableMapOf<String, RegionalCrossDockReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCrossDockReservationStore(
    var inner: RegionalCrossDockReservationStore,
    var cache: TtlCache<RegionalCrossDockReservation>,
    var hits: Int,
    var misses: Int,
) : RegionalCrossDockReservationStore {
    override fun find(code: String): RegionalCrossDockReservation? {
        val hit = (cache.get(code) as RegionalCrossDockReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCrossDockReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCrossDockReservation> = inner.list()
}

fun newCachedRegionalCrossDockReservationStore(inner: RegionalCrossDockReservationStore, ttl: Int, clock: Clock): RegionalCrossDockReservationStore {
    return CachedRegionalCrossDockReservationStore(
        inner = inner,
        cache = newTtlCache<RegionalCrossDockReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional cross dock reservations. Empty fields match all. */
data class RegionalCrossDockReservationQuery(
    var source: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCrossDockReservation): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCrossDockReservation>): MutableList<RegionalCrossDockReservation> {
        val matched = filter(items) { candidate: RegionalCrossDockReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCrossDockReservations(store: RegionalCrossDockReservationStore, query: RegionalCrossDockReservationQuery): MutableList<RegionalCrossDockReservation> {
    return query.apply(store.list())
}
