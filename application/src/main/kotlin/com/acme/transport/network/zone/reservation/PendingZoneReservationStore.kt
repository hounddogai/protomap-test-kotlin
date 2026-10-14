package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending zone reservations by code. */
interface PendingZoneReservationStore {
    fun find(code: String): PendingZoneReservation?
    fun save(item: PendingZoneReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingZoneReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingZoneReservationStore(
    var items: MutableMap<String, PendingZoneReservation>,
    var order: MutableList<String>,
) : PendingZoneReservationStore {
    override fun find(code: String): PendingZoneReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingZoneReservation) {
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

    override fun list(): MutableList<PendingZoneReservation> {
        val out = mutableListOf<PendingZoneReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingZoneReservationStore(): PendingZoneReservationStore {
    return MemoryPendingZoneReservationStore(
        mutableMapOf<String, PendingZoneReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingZoneReservationStore(
    var inner: PendingZoneReservationStore,
    var cache: TtlCache<PendingZoneReservation>,
    var hits: Int,
    var misses: Int,
) : PendingZoneReservationStore {
    override fun find(code: String): PendingZoneReservation? {
        val hit = (cache.get(code) as PendingZoneReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingZoneReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingZoneReservation> = inner.list()
}

fun newCachedPendingZoneReservationStore(inner: PendingZoneReservationStore, ttl: Int, clock: Clock): PendingZoneReservationStore {
    return CachedPendingZoneReservationStore(
        inner = inner,
        cache = newTtlCache<PendingZoneReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending zone reservations. Empty fields match all. */
data class PendingZoneReservationQuery(
    var channel: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingZoneReservation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<PendingZoneReservation>): MutableList<PendingZoneReservation> {
        val matched = filter(items) { candidate: PendingZoneReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingZoneReservations(store: PendingZoneReservationStore, query: PendingZoneReservationQuery): MutableList<PendingZoneReservation> {
    return query.apply(store.list())
}
