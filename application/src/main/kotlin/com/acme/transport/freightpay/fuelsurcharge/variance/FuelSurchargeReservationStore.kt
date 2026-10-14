package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge reservations by code. */
interface FuelSurchargeReservationStore {
    fun find(code: String): FuelSurchargeReservation?
    fun save(item: FuelSurchargeReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeReservationStore(
    var items: MutableMap<String, FuelSurchargeReservation>,
    var order: MutableList<String>,
) : FuelSurchargeReservationStore {
    override fun find(code: String): FuelSurchargeReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeReservation) {
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

    override fun list(): MutableList<FuelSurchargeReservation> {
        val out = mutableListOf<FuelSurchargeReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeReservationStore(): FuelSurchargeReservationStore {
    return MemoryFuelSurchargeReservationStore(
        mutableMapOf<String, FuelSurchargeReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeReservationStore(
    var inner: FuelSurchargeReservationStore,
    var cache: TtlCache<FuelSurchargeReservation>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeReservationStore {
    override fun find(code: String): FuelSurchargeReservation? {
        val hit = (cache.get(code) as FuelSurchargeReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeReservation> = inner.list()
}

fun newCachedFuelSurchargeReservationStore(inner: FuelSurchargeReservationStore, ttl: Int, clock: Clock): FuelSurchargeReservationStore {
    return CachedFuelSurchargeReservationStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge reservations. Empty fields match all. */
data class FuelSurchargeReservationQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeReservation>): MutableList<FuelSurchargeReservation> {
        val matched = filter(items) { candidate: FuelSurchargeReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeReservations(store: FuelSurchargeReservationStore, query: FuelSurchargeReservationQuery): MutableList<FuelSurchargeReservation> {
    return query.apply(store.list())
}
