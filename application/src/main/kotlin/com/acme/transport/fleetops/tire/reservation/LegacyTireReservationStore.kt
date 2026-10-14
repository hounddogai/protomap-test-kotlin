package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy tire reservations by code. */
interface LegacyTireReservationStore {
    fun find(code: String): LegacyTireReservation?
    fun save(item: LegacyTireReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTireReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTireReservationStore(
    var items: MutableMap<String, LegacyTireReservation>,
    var order: MutableList<String>,
) : LegacyTireReservationStore {
    override fun find(code: String): LegacyTireReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTireReservation) {
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

    override fun list(): MutableList<LegacyTireReservation> {
        val out = mutableListOf<LegacyTireReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTireReservationStore(): LegacyTireReservationStore {
    return MemoryLegacyTireReservationStore(
        mutableMapOf<String, LegacyTireReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTireReservationStore(
    var inner: LegacyTireReservationStore,
    var cache: TtlCache<LegacyTireReservation>,
    var hits: Int,
    var misses: Int,
) : LegacyTireReservationStore {
    override fun find(code: String): LegacyTireReservation? {
        val hit = (cache.get(code) as LegacyTireReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTireReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTireReservation> = inner.list()
}

fun newCachedLegacyTireReservationStore(inner: LegacyTireReservationStore, ttl: Int, clock: Clock): LegacyTireReservationStore {
    return CachedLegacyTireReservationStore(
        inner = inner,
        cache = newTtlCache<LegacyTireReservation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy tire reservations. Empty fields match all. */
data class LegacyTireReservationQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTireReservation): Boolean {
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

    fun apply(items: MutableList<LegacyTireReservation>): MutableList<LegacyTireReservation> {
        val matched = filter(items) { candidate: LegacyTireReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTireReservations(store: LegacyTireReservationStore, query: LegacyTireReservationQuery): MutableList<LegacyTireReservation> {
    return query.apply(store.list())
}
