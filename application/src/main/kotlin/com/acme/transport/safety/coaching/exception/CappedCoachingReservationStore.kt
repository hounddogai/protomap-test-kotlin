package com.acme.transport.safety.coaching.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped coaching reservations by code. */
interface CappedCoachingReservationStore {
    fun find(code: String): CappedCoachingReservation?
    fun save(item: CappedCoachingReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCoachingReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCoachingReservationStore(
    var items: MutableMap<String, CappedCoachingReservation>,
    var order: MutableList<String>,
) : CappedCoachingReservationStore {
    override fun find(code: String): CappedCoachingReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCoachingReservation) {
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

    override fun list(): MutableList<CappedCoachingReservation> {
        val out = mutableListOf<CappedCoachingReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCoachingReservationStore(): CappedCoachingReservationStore {
    return MemoryCappedCoachingReservationStore(
        mutableMapOf<String, CappedCoachingReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCoachingReservationStore(
    var inner: CappedCoachingReservationStore,
    var cache: TtlCache<CappedCoachingReservation>,
    var hits: Int,
    var misses: Int,
) : CappedCoachingReservationStore {
    override fun find(code: String): CappedCoachingReservation? {
        val hit = (cache.get(code) as CappedCoachingReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCoachingReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCoachingReservation> = inner.list()
}

fun newCachedCappedCoachingReservationStore(inner: CappedCoachingReservationStore, ttl: Int, clock: Clock): CappedCoachingReservationStore {
    return CachedCappedCoachingReservationStore(
        inner = inner,
        cache = newTtlCache<CappedCoachingReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped coaching reservations. Empty fields match all. */
data class CappedCoachingReservationQuery(
    var category: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCoachingReservation): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedCoachingReservation>): MutableList<CappedCoachingReservation> {
        val matched = filter(items) { candidate: CappedCoachingReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCoachingReservations(store: CappedCoachingReservationStore, query: CappedCoachingReservationQuery): MutableList<CappedCoachingReservation> {
    return query.apply(store.list())
}
