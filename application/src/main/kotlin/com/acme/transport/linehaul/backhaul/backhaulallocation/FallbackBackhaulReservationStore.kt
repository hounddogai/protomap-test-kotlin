package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback backhaul reservations by code. */
interface FallbackBackhaulReservationStore {
    fun find(code: String): FallbackBackhaulReservation?
    fun save(item: FallbackBackhaulReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackBackhaulReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackBackhaulReservationStore(
    var items: MutableMap<String, FallbackBackhaulReservation>,
    var order: MutableList<String>,
) : FallbackBackhaulReservationStore {
    override fun find(code: String): FallbackBackhaulReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackBackhaulReservation) {
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

    override fun list(): MutableList<FallbackBackhaulReservation> {
        val out = mutableListOf<FallbackBackhaulReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackBackhaulReservationStore(): FallbackBackhaulReservationStore {
    return MemoryFallbackBackhaulReservationStore(
        mutableMapOf<String, FallbackBackhaulReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackBackhaulReservationStore(
    var inner: FallbackBackhaulReservationStore,
    var cache: TtlCache<FallbackBackhaulReservation>,
    var hits: Int,
    var misses: Int,
) : FallbackBackhaulReservationStore {
    override fun find(code: String): FallbackBackhaulReservation? {
        val hit = (cache.get(code) as FallbackBackhaulReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackBackhaulReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackBackhaulReservation> = inner.list()
}

fun newCachedFallbackBackhaulReservationStore(inner: FallbackBackhaulReservationStore, ttl: Int, clock: Clock): FallbackBackhaulReservationStore {
    return CachedFallbackBackhaulReservationStore(
        inner = inner,
        cache = newTtlCache<FallbackBackhaulReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback backhaul reservations. Empty fields match all. */
data class FallbackBackhaulReservationQuery(
    var region: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackBackhaulReservation): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackBackhaulReservation>): MutableList<FallbackBackhaulReservation> {
        val matched = filter(items) { candidate: FallbackBackhaulReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackBackhaulReservations(store: FallbackBackhaulReservationStore, query: FallbackBackhaulReservationQuery): MutableList<FallbackBackhaulReservation> {
    return query.apply(store.list())
}
