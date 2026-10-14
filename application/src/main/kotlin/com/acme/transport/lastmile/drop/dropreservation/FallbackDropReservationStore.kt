package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback drop reservations by code. */
interface FallbackDropReservationStore {
    fun find(code: String): FallbackDropReservation?
    fun save(item: FallbackDropReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDropReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDropReservationStore(
    var items: MutableMap<String, FallbackDropReservation>,
    var order: MutableList<String>,
) : FallbackDropReservationStore {
    override fun find(code: String): FallbackDropReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDropReservation) {
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

    override fun list(): MutableList<FallbackDropReservation> {
        val out = mutableListOf<FallbackDropReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDropReservationStore(): FallbackDropReservationStore {
    return MemoryFallbackDropReservationStore(
        mutableMapOf<String, FallbackDropReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDropReservationStore(
    var inner: FallbackDropReservationStore,
    var cache: TtlCache<FallbackDropReservation>,
    var hits: Int,
    var misses: Int,
) : FallbackDropReservationStore {
    override fun find(code: String): FallbackDropReservation? {
        val hit = (cache.get(code) as FallbackDropReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDropReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDropReservation> = inner.list()
}

fun newCachedFallbackDropReservationStore(inner: FallbackDropReservationStore, ttl: Int, clock: Clock): FallbackDropReservationStore {
    return CachedFallbackDropReservationStore(
        inner = inner,
        cache = newTtlCache<FallbackDropReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback drop reservations. Empty fields match all. */
data class FallbackDropReservationQuery(
    var category: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDropReservation): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDropReservation>): MutableList<FallbackDropReservation> {
        val matched = filter(items) { candidate: FallbackDropReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDropReservations(store: FallbackDropReservationStore, query: FallbackDropReservationQuery): MutableList<FallbackDropReservation> {
    return query.apply(store.list())
}
