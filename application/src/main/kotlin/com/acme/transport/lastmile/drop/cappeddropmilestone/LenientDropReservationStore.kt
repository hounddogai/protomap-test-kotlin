package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient drop reservations by code. */
interface LenientDropReservationStore {
    fun find(code: String): LenientDropReservation?
    fun save(item: LenientDropReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDropReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDropReservationStore(
    var items: MutableMap<String, LenientDropReservation>,
    var order: MutableList<String>,
) : LenientDropReservationStore {
    override fun find(code: String): LenientDropReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDropReservation) {
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

    override fun list(): MutableList<LenientDropReservation> {
        val out = mutableListOf<LenientDropReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDropReservationStore(): LenientDropReservationStore {
    return MemoryLenientDropReservationStore(
        mutableMapOf<String, LenientDropReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDropReservationStore(
    var inner: LenientDropReservationStore,
    var cache: TtlCache<LenientDropReservation>,
    var hits: Int,
    var misses: Int,
) : LenientDropReservationStore {
    override fun find(code: String): LenientDropReservation? {
        val hit = (cache.get(code) as LenientDropReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDropReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDropReservation> = inner.list()
}

fun newCachedLenientDropReservationStore(inner: LenientDropReservationStore, ttl: Int, clock: Clock): LenientDropReservationStore {
    return CachedLenientDropReservationStore(
        inner = inner,
        cache = newTtlCache<LenientDropReservation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient drop reservations. Empty fields match all. */
data class LenientDropReservationQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDropReservation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDropReservation>): MutableList<LenientDropReservation> {
        val matched = filter(items) { candidate: LenientDropReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDropReservations(store: LenientDropReservationStore, query: LenientDropReservationQuery): MutableList<LenientDropReservation> {
    return query.apply(store.list())
}
