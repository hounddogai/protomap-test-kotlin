package com.acme.transport.linehaul.capacity.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched capacity reservations by code. */
interface BatchedCapacityReservationStore {
    fun find(code: String): BatchedCapacityReservation?
    fun save(item: BatchedCapacityReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCapacityReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCapacityReservationStore(
    var items: MutableMap<String, BatchedCapacityReservation>,
    var order: MutableList<String>,
) : BatchedCapacityReservationStore {
    override fun find(code: String): BatchedCapacityReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCapacityReservation) {
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

    override fun list(): MutableList<BatchedCapacityReservation> {
        val out = mutableListOf<BatchedCapacityReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCapacityReservationStore(): BatchedCapacityReservationStore {
    return MemoryBatchedCapacityReservationStore(
        mutableMapOf<String, BatchedCapacityReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCapacityReservationStore(
    var inner: BatchedCapacityReservationStore,
    var cache: TtlCache<BatchedCapacityReservation>,
    var hits: Int,
    var misses: Int,
) : BatchedCapacityReservationStore {
    override fun find(code: String): BatchedCapacityReservation? {
        val hit = (cache.get(code) as BatchedCapacityReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCapacityReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCapacityReservation> = inner.list()
}

fun newCachedBatchedCapacityReservationStore(inner: BatchedCapacityReservationStore, ttl: Int, clock: Clock): BatchedCapacityReservationStore {
    return CachedBatchedCapacityReservationStore(
        inner = inner,
        cache = newTtlCache<BatchedCapacityReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched capacity reservations. Empty fields match all. */
data class BatchedCapacityReservationQuery(
    var segment: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCapacityReservation): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedCapacityReservation>): MutableList<BatchedCapacityReservation> {
        val matched = filter(items) { candidate: BatchedCapacityReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCapacityReservations(store: BatchedCapacityReservationStore, query: BatchedCapacityReservationQuery): MutableList<BatchedCapacityReservation> {
    return query.apply(store.list())
}
