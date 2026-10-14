package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery reservations by code. */
interface RedeliveryReservationStore {
    fun find(code: String): RedeliveryReservation?
    fun save(item: RedeliveryReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryReservationStore(
    var items: MutableMap<String, RedeliveryReservation>,
    var order: MutableList<String>,
) : RedeliveryReservationStore {
    override fun find(code: String): RedeliveryReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryReservation) {
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

    override fun list(): MutableList<RedeliveryReservation> {
        val out = mutableListOf<RedeliveryReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryReservationStore(): RedeliveryReservationStore {
    return MemoryRedeliveryReservationStore(
        mutableMapOf<String, RedeliveryReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryReservationStore(
    var inner: RedeliveryReservationStore,
    var cache: TtlCache<RedeliveryReservation>,
    var hits: Int,
    var misses: Int,
) : RedeliveryReservationStore {
    override fun find(code: String): RedeliveryReservation? {
        val hit = (cache.get(code) as RedeliveryReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryReservation> = inner.list()
}

fun newCachedRedeliveryReservationStore(inner: RedeliveryReservationStore, ttl: Int, clock: Clock): RedeliveryReservationStore {
    return CachedRedeliveryReservationStore(
        inner = inner,
        cache = newTtlCache<RedeliveryReservation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery reservations. Empty fields match all. */
data class RedeliveryReservationQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryReservation): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryReservation>): MutableList<RedeliveryReservation> {
        val matched = filter(items) { candidate: RedeliveryReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryReservations(store: RedeliveryReservationStore, query: RedeliveryReservationQuery): MutableList<RedeliveryReservation> {
    return query.apply(store.list())
}
