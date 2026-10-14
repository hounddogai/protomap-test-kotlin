package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered zone reservations by code. */
interface TieredZoneReservationStore {
    fun find(code: String): TieredZoneReservation?
    fun save(item: TieredZoneReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredZoneReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredZoneReservationStore(
    var items: MutableMap<String, TieredZoneReservation>,
    var order: MutableList<String>,
) : TieredZoneReservationStore {
    override fun find(code: String): TieredZoneReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredZoneReservation) {
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

    override fun list(): MutableList<TieredZoneReservation> {
        val out = mutableListOf<TieredZoneReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredZoneReservationStore(): TieredZoneReservationStore {
    return MemoryTieredZoneReservationStore(
        mutableMapOf<String, TieredZoneReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredZoneReservationStore(
    var inner: TieredZoneReservationStore,
    var cache: TtlCache<TieredZoneReservation>,
    var hits: Int,
    var misses: Int,
) : TieredZoneReservationStore {
    override fun find(code: String): TieredZoneReservation? {
        val hit = (cache.get(code) as TieredZoneReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredZoneReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredZoneReservation> = inner.list()
}

fun newCachedTieredZoneReservationStore(inner: TieredZoneReservationStore, ttl: Int, clock: Clock): TieredZoneReservationStore {
    return CachedTieredZoneReservationStore(
        inner = inner,
        cache = newTtlCache<TieredZoneReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered zone reservations. Empty fields match all. */
data class TieredZoneReservationQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredZoneReservation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TieredZoneReservation>): MutableList<TieredZoneReservation> {
        val matched = filter(items) { candidate: TieredZoneReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredZoneReservations(store: TieredZoneReservationStore, query: TieredZoneReservationQuery): MutableList<TieredZoneReservation> {
    return query.apply(store.list())
}
