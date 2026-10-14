package com.acme.transport.crewing.swap.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy swap reservations by code. */
interface LegacySwapReservationStore {
    fun find(code: String): LegacySwapReservation?
    fun save(item: LegacySwapReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacySwapReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacySwapReservationStore(
    var items: MutableMap<String, LegacySwapReservation>,
    var order: MutableList<String>,
) : LegacySwapReservationStore {
    override fun find(code: String): LegacySwapReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacySwapReservation) {
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

    override fun list(): MutableList<LegacySwapReservation> {
        val out = mutableListOf<LegacySwapReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacySwapReservationStore(): LegacySwapReservationStore {
    return MemoryLegacySwapReservationStore(
        mutableMapOf<String, LegacySwapReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacySwapReservationStore(
    var inner: LegacySwapReservationStore,
    var cache: TtlCache<LegacySwapReservation>,
    var hits: Int,
    var misses: Int,
) : LegacySwapReservationStore {
    override fun find(code: String): LegacySwapReservation? {
        val hit = (cache.get(code) as LegacySwapReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacySwapReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacySwapReservation> = inner.list()
}

fun newCachedLegacySwapReservationStore(inner: LegacySwapReservationStore, ttl: Int, clock: Clock): LegacySwapReservationStore {
    return CachedLegacySwapReservationStore(
        inner = inner,
        cache = newTtlCache<LegacySwapReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy swap reservations. Empty fields match all. */
data class LegacySwapReservationQuery(
    var country: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacySwapReservation): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacySwapReservation>): MutableList<LegacySwapReservation> {
        val matched = filter(items) { candidate: LegacySwapReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacySwapReservations(store: LegacySwapReservationStore, query: LegacySwapReservationQuery): MutableList<LegacySwapReservation> {
    return query.apply(store.list())
}
