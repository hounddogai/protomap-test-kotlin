package com.acme.transport.linehaul.capacity.capacityreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered capacity reservations by code. */
interface TieredCapacityReservationStore {
    fun find(code: String): TieredCapacityReservation?
    fun save(item: TieredCapacityReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCapacityReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCapacityReservationStore(
    var items: MutableMap<String, TieredCapacityReservation>,
    var order: MutableList<String>,
) : TieredCapacityReservationStore {
    override fun find(code: String): TieredCapacityReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCapacityReservation) {
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

    override fun list(): MutableList<TieredCapacityReservation> {
        val out = mutableListOf<TieredCapacityReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCapacityReservationStore(): TieredCapacityReservationStore {
    return MemoryTieredCapacityReservationStore(
        mutableMapOf<String, TieredCapacityReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCapacityReservationStore(
    var inner: TieredCapacityReservationStore,
    var cache: TtlCache<TieredCapacityReservation>,
    var hits: Int,
    var misses: Int,
) : TieredCapacityReservationStore {
    override fun find(code: String): TieredCapacityReservation? {
        val hit = (cache.get(code) as TieredCapacityReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCapacityReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCapacityReservation> = inner.list()
}

fun newCachedTieredCapacityReservationStore(inner: TieredCapacityReservationStore, ttl: Int, clock: Clock): TieredCapacityReservationStore {
    return CachedTieredCapacityReservationStore(
        inner = inner,
        cache = newTtlCache<TieredCapacityReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered capacity reservations. Empty fields match all. */
data class TieredCapacityReservationQuery(
    var country: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCapacityReservation): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCapacityReservation>): MutableList<TieredCapacityReservation> {
        val matched = filter(items) { candidate: TieredCapacityReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCapacityReservations(store: TieredCapacityReservationStore, query: TieredCapacityReservationQuery): MutableList<TieredCapacityReservation> {
    return query.apply(store.list())
}
