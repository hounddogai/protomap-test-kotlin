package com.acme.transport.freightpay.dispute.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global dispute reservations by code. */
interface GlobalDisputeReservationStore {
    fun find(code: String): GlobalDisputeReservation?
    fun save(item: GlobalDisputeReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDisputeReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDisputeReservationStore(
    var items: MutableMap<String, GlobalDisputeReservation>,
    var order: MutableList<String>,
) : GlobalDisputeReservationStore {
    override fun find(code: String): GlobalDisputeReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDisputeReservation) {
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

    override fun list(): MutableList<GlobalDisputeReservation> {
        val out = mutableListOf<GlobalDisputeReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDisputeReservationStore(): GlobalDisputeReservationStore {
    return MemoryGlobalDisputeReservationStore(
        mutableMapOf<String, GlobalDisputeReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDisputeReservationStore(
    var inner: GlobalDisputeReservationStore,
    var cache: TtlCache<GlobalDisputeReservation>,
    var hits: Int,
    var misses: Int,
) : GlobalDisputeReservationStore {
    override fun find(code: String): GlobalDisputeReservation? {
        val hit = (cache.get(code) as GlobalDisputeReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDisputeReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDisputeReservation> = inner.list()
}

fun newCachedGlobalDisputeReservationStore(inner: GlobalDisputeReservationStore, ttl: Int, clock: Clock): GlobalDisputeReservationStore {
    return CachedGlobalDisputeReservationStore(
        inner = inner,
        cache = newTtlCache<GlobalDisputeReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global dispute reservations. Empty fields match all. */
data class GlobalDisputeReservationQuery(
    var region: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDisputeReservation): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalDisputeReservation>): MutableList<GlobalDisputeReservation> {
        val matched = filter(items) { candidate: GlobalDisputeReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDisputeReservations(store: GlobalDisputeReservationStore, query: GlobalDisputeReservationQuery): MutableList<GlobalDisputeReservation> {
    return query.apply(store.list())
}
