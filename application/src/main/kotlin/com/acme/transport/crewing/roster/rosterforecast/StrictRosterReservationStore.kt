package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict roster reservations by code. */
interface StrictRosterReservationStore {
    fun find(code: String): StrictRosterReservation?
    fun save(item: StrictRosterReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRosterReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRosterReservationStore(
    var items: MutableMap<String, StrictRosterReservation>,
    var order: MutableList<String>,
) : StrictRosterReservationStore {
    override fun find(code: String): StrictRosterReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRosterReservation) {
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

    override fun list(): MutableList<StrictRosterReservation> {
        val out = mutableListOf<StrictRosterReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRosterReservationStore(): StrictRosterReservationStore {
    return MemoryStrictRosterReservationStore(
        mutableMapOf<String, StrictRosterReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRosterReservationStore(
    var inner: StrictRosterReservationStore,
    var cache: TtlCache<StrictRosterReservation>,
    var hits: Int,
    var misses: Int,
) : StrictRosterReservationStore {
    override fun find(code: String): StrictRosterReservation? {
        val hit = (cache.get(code) as StrictRosterReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRosterReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRosterReservation> = inner.list()
}

fun newCachedStrictRosterReservationStore(inner: StrictRosterReservationStore, ttl: Int, clock: Clock): StrictRosterReservationStore {
    return CachedStrictRosterReservationStore(
        inner = inner,
        cache = newTtlCache<StrictRosterReservation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict roster reservations. Empty fields match all. */
data class StrictRosterReservationQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRosterReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRosterReservation>): MutableList<StrictRosterReservation> {
        val matched = filter(items) { candidate: StrictRosterReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRosterReservations(store: StrictRosterReservationStore, query: StrictRosterReservationQuery): MutableList<StrictRosterReservation> {
    return query.apply(store.list())
}
