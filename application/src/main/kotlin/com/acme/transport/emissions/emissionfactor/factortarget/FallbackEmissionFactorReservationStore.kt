package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback emission factor reservations by code. */
interface FallbackEmissionFactorReservationStore {
    fun find(code: String): FallbackEmissionFactorReservation?
    fun save(item: FallbackEmissionFactorReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackEmissionFactorReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackEmissionFactorReservationStore(
    var items: MutableMap<String, FallbackEmissionFactorReservation>,
    var order: MutableList<String>,
) : FallbackEmissionFactorReservationStore {
    override fun find(code: String): FallbackEmissionFactorReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackEmissionFactorReservation) {
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

    override fun list(): MutableList<FallbackEmissionFactorReservation> {
        val out = mutableListOf<FallbackEmissionFactorReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackEmissionFactorReservationStore(): FallbackEmissionFactorReservationStore {
    return MemoryFallbackEmissionFactorReservationStore(
        mutableMapOf<String, FallbackEmissionFactorReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackEmissionFactorReservationStore(
    var inner: FallbackEmissionFactorReservationStore,
    var cache: TtlCache<FallbackEmissionFactorReservation>,
    var hits: Int,
    var misses: Int,
) : FallbackEmissionFactorReservationStore {
    override fun find(code: String): FallbackEmissionFactorReservation? {
        val hit = (cache.get(code) as FallbackEmissionFactorReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackEmissionFactorReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackEmissionFactorReservation> = inner.list()
}

fun newCachedFallbackEmissionFactorReservationStore(inner: FallbackEmissionFactorReservationStore, ttl: Int, clock: Clock): FallbackEmissionFactorReservationStore {
    return CachedFallbackEmissionFactorReservationStore(
        inner = inner,
        cache = newTtlCache<FallbackEmissionFactorReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback emission factor reservations. Empty fields match all. */
data class FallbackEmissionFactorReservationQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackEmissionFactorReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackEmissionFactorReservation>): MutableList<FallbackEmissionFactorReservation> {
        val matched = filter(items) { candidate: FallbackEmissionFactorReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackEmissionFactorReservations(store: FallbackEmissionFactorReservationStore, query: FallbackEmissionFactorReservationQuery): MutableList<FallbackEmissionFactorReservation> {
    return query.apply(store.list())
}
