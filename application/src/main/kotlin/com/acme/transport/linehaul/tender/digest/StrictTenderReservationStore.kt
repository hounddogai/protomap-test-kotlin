package com.acme.transport.linehaul.tender.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict tender reservations by code. */
interface StrictTenderReservationStore {
    fun find(code: String): StrictTenderReservation?
    fun save(item: StrictTenderReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTenderReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTenderReservationStore(
    var items: MutableMap<String, StrictTenderReservation>,
    var order: MutableList<String>,
) : StrictTenderReservationStore {
    override fun find(code: String): StrictTenderReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTenderReservation) {
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

    override fun list(): MutableList<StrictTenderReservation> {
        val out = mutableListOf<StrictTenderReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTenderReservationStore(): StrictTenderReservationStore {
    return MemoryStrictTenderReservationStore(
        mutableMapOf<String, StrictTenderReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTenderReservationStore(
    var inner: StrictTenderReservationStore,
    var cache: TtlCache<StrictTenderReservation>,
    var hits: Int,
    var misses: Int,
) : StrictTenderReservationStore {
    override fun find(code: String): StrictTenderReservation? {
        val hit = (cache.get(code) as StrictTenderReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTenderReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTenderReservation> = inner.list()
}

fun newCachedStrictTenderReservationStore(inner: StrictTenderReservationStore, ttl: Int, clock: Clock): StrictTenderReservationStore {
    return CachedStrictTenderReservationStore(
        inner = inner,
        cache = newTtlCache<StrictTenderReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict tender reservations. Empty fields match all. */
data class StrictTenderReservationQuery(
    var lane: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTenderReservation): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<StrictTenderReservation>): MutableList<StrictTenderReservation> {
        val matched = filter(items) { candidate: StrictTenderReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTenderReservations(store: StrictTenderReservationStore, query: StrictTenderReservationQuery): MutableList<StrictTenderReservation> {
    return query.apply(store.list())
}
