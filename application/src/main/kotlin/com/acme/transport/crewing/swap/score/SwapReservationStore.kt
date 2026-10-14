package com.acme.transport.crewing.swap.score

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap reservations by code. */
interface SwapReservationStore {
    fun find(code: String): SwapReservation?
    fun save(item: SwapReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapReservationStore(
    var items: MutableMap<String, SwapReservation>,
    var order: MutableList<String>,
) : SwapReservationStore {
    override fun find(code: String): SwapReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapReservation) {
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

    override fun list(): MutableList<SwapReservation> {
        val out = mutableListOf<SwapReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapReservationStore(): SwapReservationStore {
    return MemorySwapReservationStore(mutableMapOf<String, SwapReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapReservationStore(
    var inner: SwapReservationStore,
    var cache: TtlCache<SwapReservation>,
    var hits: Int,
    var misses: Int,
) : SwapReservationStore {
    override fun find(code: String): SwapReservation? {
        val hit = (cache.get(code) as SwapReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapReservation> = inner.list()
}

fun newCachedSwapReservationStore(inner: SwapReservationStore, ttl: Int, clock: Clock): SwapReservationStore {
    return CachedSwapReservationStore(
        inner = inner,
        cache = newTtlCache<SwapReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap reservations. Empty fields match all. */
data class SwapReservationQuery(
    var locale: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapReservation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapReservation>): MutableList<SwapReservation> {
        val matched = filter(items) { candidate: SwapReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapReservations(store: SwapReservationStore, query: SwapReservationQuery): MutableList<SwapReservation> {
    return query.apply(store.list())
}
