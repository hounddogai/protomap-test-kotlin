package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient dispute reservations by code. */
interface LenientDisputeReservationStore {
    fun find(code: String): LenientDisputeReservation?
    fun save(item: LenientDisputeReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDisputeReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDisputeReservationStore(
    var items: MutableMap<String, LenientDisputeReservation>,
    var order: MutableList<String>,
) : LenientDisputeReservationStore {
    override fun find(code: String): LenientDisputeReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDisputeReservation) {
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

    override fun list(): MutableList<LenientDisputeReservation> {
        val out = mutableListOf<LenientDisputeReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDisputeReservationStore(): LenientDisputeReservationStore {
    return MemoryLenientDisputeReservationStore(
        mutableMapOf<String, LenientDisputeReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDisputeReservationStore(
    var inner: LenientDisputeReservationStore,
    var cache: TtlCache<LenientDisputeReservation>,
    var hits: Int,
    var misses: Int,
) : LenientDisputeReservationStore {
    override fun find(code: String): LenientDisputeReservation? {
        val hit = (cache.get(code) as LenientDisputeReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDisputeReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDisputeReservation> = inner.list()
}

fun newCachedLenientDisputeReservationStore(inner: LenientDisputeReservationStore, ttl: Int, clock: Clock): LenientDisputeReservationStore {
    return CachedLenientDisputeReservationStore(
        inner = inner,
        cache = newTtlCache<LenientDisputeReservation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dispute reservations. Empty fields match all. */
data class LenientDisputeReservationQuery(
    var source: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDisputeReservation): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDisputeReservation>): MutableList<LenientDisputeReservation> {
        val matched = filter(items) { candidate: LenientDisputeReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDisputeReservations(store: LenientDisputeReservationStore, query: LenientDisputeReservationQuery): MutableList<LenientDisputeReservation> {
    return query.apply(store.list())
}
