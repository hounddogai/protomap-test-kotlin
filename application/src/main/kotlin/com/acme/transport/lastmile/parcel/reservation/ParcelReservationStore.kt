package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel reservations by code. */
interface ParcelReservationStore {
    fun find(code: String): ParcelReservation?
    fun save(item: ParcelReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelReservationStore(
    var items: MutableMap<String, ParcelReservation>,
    var order: MutableList<String>,
) : ParcelReservationStore {
    override fun find(code: String): ParcelReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelReservation) {
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

    override fun list(): MutableList<ParcelReservation> {
        val out = mutableListOf<ParcelReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelReservationStore(): ParcelReservationStore {
    return MemoryParcelReservationStore(mutableMapOf<String, ParcelReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelReservationStore(
    var inner: ParcelReservationStore,
    var cache: TtlCache<ParcelReservation>,
    var hits: Int,
    var misses: Int,
) : ParcelReservationStore {
    override fun find(code: String): ParcelReservation? {
        val hit = (cache.get(code) as ParcelReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelReservation> = inner.list()
}

fun newCachedParcelReservationStore(inner: ParcelReservationStore, ttl: Int, clock: Clock): ParcelReservationStore {
    return CachedParcelReservationStore(
        inner = inner,
        cache = newTtlCache<ParcelReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel reservations. Empty fields match all. */
data class ParcelReservationQuery(
    var segment: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelReservation): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelReservation>): MutableList<ParcelReservation> {
        val matched = filter(items) { candidate: ParcelReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelReservations(store: ParcelReservationStore, query: ParcelReservationQuery): MutableList<ParcelReservation> {
    return query.apply(store.list())
}
