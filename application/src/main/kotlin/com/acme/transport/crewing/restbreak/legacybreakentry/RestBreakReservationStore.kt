package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break reservations by code. */
interface RestBreakReservationStore {
    fun find(code: String): RestBreakReservation?
    fun save(item: RestBreakReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakReservationStore(
    var items: MutableMap<String, RestBreakReservation>,
    var order: MutableList<String>,
) : RestBreakReservationStore {
    override fun find(code: String): RestBreakReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakReservation) {
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

    override fun list(): MutableList<RestBreakReservation> {
        val out = mutableListOf<RestBreakReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakReservationStore(): RestBreakReservationStore {
    return MemoryRestBreakReservationStore(
        mutableMapOf<String, RestBreakReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakReservationStore(
    var inner: RestBreakReservationStore,
    var cache: TtlCache<RestBreakReservation>,
    var hits: Int,
    var misses: Int,
) : RestBreakReservationStore {
    override fun find(code: String): RestBreakReservation? {
        val hit = (cache.get(code) as RestBreakReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakReservation> = inner.list()
}

fun newCachedRestBreakReservationStore(inner: RestBreakReservationStore, ttl: Int, clock: Clock): RestBreakReservationStore {
    return CachedRestBreakReservationStore(
        inner = inner,
        cache = newTtlCache<RestBreakReservation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break reservations. Empty fields match all. */
data class RestBreakReservationQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakReservation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakReservation>): MutableList<RestBreakReservation> {
        val matched = filter(items) { candidate: RestBreakReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakReservations(store: RestBreakReservationStore, query: RestBreakReservationQuery): MutableList<RestBreakReservation> {
    return query.apply(store.list())
}
