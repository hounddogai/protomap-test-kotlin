package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn reservations by code. */
interface FuelBurnReservationStore {
    fun find(code: String): FuelBurnReservation?
    fun save(item: FuelBurnReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnReservationStore(
    var items: MutableMap<String, FuelBurnReservation>,
    var order: MutableList<String>,
) : FuelBurnReservationStore {
    override fun find(code: String): FuelBurnReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnReservation) {
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

    override fun list(): MutableList<FuelBurnReservation> {
        val out = mutableListOf<FuelBurnReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnReservationStore(): FuelBurnReservationStore {
    return MemoryFuelBurnReservationStore(mutableMapOf<String, FuelBurnReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnReservationStore(
    var inner: FuelBurnReservationStore,
    var cache: TtlCache<FuelBurnReservation>,
    var hits: Int,
    var misses: Int,
) : FuelBurnReservationStore {
    override fun find(code: String): FuelBurnReservation? {
        val hit = (cache.get(code) as FuelBurnReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnReservation> = inner.list()
}

fun newCachedFuelBurnReservationStore(inner: FuelBurnReservationStore, ttl: Int, clock: Clock): FuelBurnReservationStore {
    return CachedFuelBurnReservationStore(
        inner = inner,
        cache = newTtlCache<FuelBurnReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn reservations. Empty fields match all. */
data class FuelBurnReservationQuery(
    var channel: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnReservation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnReservation>): MutableList<FuelBurnReservation> {
        val matched = filter(items) { candidate: FuelBurnReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnReservations(store: FuelBurnReservationStore, query: FuelBurnReservationQuery): MutableList<FuelBurnReservation> {
    return query.apply(store.list())
}
