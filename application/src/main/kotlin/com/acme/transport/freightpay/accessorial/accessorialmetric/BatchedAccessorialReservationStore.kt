package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched accessorial reservations by code. */
interface BatchedAccessorialReservationStore {
    fun find(code: String): BatchedAccessorialReservation?
    fun save(item: BatchedAccessorialReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAccessorialReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAccessorialReservationStore(
    var items: MutableMap<String, BatchedAccessorialReservation>,
    var order: MutableList<String>,
) : BatchedAccessorialReservationStore {
    override fun find(code: String): BatchedAccessorialReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAccessorialReservation) {
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

    override fun list(): MutableList<BatchedAccessorialReservation> {
        val out = mutableListOf<BatchedAccessorialReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAccessorialReservationStore(): BatchedAccessorialReservationStore {
    return MemoryBatchedAccessorialReservationStore(
        mutableMapOf<String, BatchedAccessorialReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAccessorialReservationStore(
    var inner: BatchedAccessorialReservationStore,
    var cache: TtlCache<BatchedAccessorialReservation>,
    var hits: Int,
    var misses: Int,
) : BatchedAccessorialReservationStore {
    override fun find(code: String): BatchedAccessorialReservation? {
        val hit = (cache.get(code) as BatchedAccessorialReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAccessorialReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAccessorialReservation> = inner.list()
}

fun newCachedBatchedAccessorialReservationStore(inner: BatchedAccessorialReservationStore, ttl: Int, clock: Clock): BatchedAccessorialReservationStore {
    return CachedBatchedAccessorialReservationStore(
        inner = inner,
        cache = newTtlCache<BatchedAccessorialReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched accessorial reservations. Empty fields match all. */
data class BatchedAccessorialReservationQuery(
    var region: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAccessorialReservation): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAccessorialReservation>): MutableList<BatchedAccessorialReservation> {
        val matched = filter(items) { candidate: BatchedAccessorialReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAccessorialReservations(store: BatchedAccessorialReservationStore, query: BatchedAccessorialReservationQuery): MutableList<BatchedAccessorialReservation> {
    return query.apply(store.list())
}
