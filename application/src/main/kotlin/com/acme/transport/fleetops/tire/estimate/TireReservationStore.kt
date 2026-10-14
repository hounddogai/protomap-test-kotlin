package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire reservations by code. */
interface TireReservationStore {
    fun find(code: String): TireReservation?
    fun save(item: TireReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireReservationStore(
    var items: MutableMap<String, TireReservation>,
    var order: MutableList<String>,
) : TireReservationStore {
    override fun find(code: String): TireReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireReservation) {
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

    override fun list(): MutableList<TireReservation> {
        val out = mutableListOf<TireReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireReservationStore(): TireReservationStore {
    return MemoryTireReservationStore(mutableMapOf<String, TireReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireReservationStore(
    var inner: TireReservationStore,
    var cache: TtlCache<TireReservation>,
    var hits: Int,
    var misses: Int,
) : TireReservationStore {
    override fun find(code: String): TireReservation? {
        val hit = (cache.get(code) as TireReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireReservation> = inner.list()
}

fun newCachedTireReservationStore(inner: TireReservationStore, ttl: Int, clock: Clock): TireReservationStore {
    return CachedTireReservationStore(
        inner = inner,
        cache = newTtlCache<TireReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire reservations. Empty fields match all. */
data class TireReservationQuery(
    var title: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireReservation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireReservation>): MutableList<TireReservation> {
        val matched = filter(items) { candidate: TireReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireReservations(store: TireReservationStore, query: TireReservationQuery): MutableList<TireReservation> {
    return query.apply(store.list())
}
