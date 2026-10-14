package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched service plan reservations by code. */
interface BatchedServicePlanReservationStore {
    fun find(code: String): BatchedServicePlanReservation?
    fun save(item: BatchedServicePlanReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedServicePlanReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedServicePlanReservationStore(
    var items: MutableMap<String, BatchedServicePlanReservation>,
    var order: MutableList<String>,
) : BatchedServicePlanReservationStore {
    override fun find(code: String): BatchedServicePlanReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedServicePlanReservation) {
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

    override fun list(): MutableList<BatchedServicePlanReservation> {
        val out = mutableListOf<BatchedServicePlanReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedServicePlanReservationStore(): BatchedServicePlanReservationStore {
    return MemoryBatchedServicePlanReservationStore(
        mutableMapOf<String, BatchedServicePlanReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedServicePlanReservationStore(
    var inner: BatchedServicePlanReservationStore,
    var cache: TtlCache<BatchedServicePlanReservation>,
    var hits: Int,
    var misses: Int,
) : BatchedServicePlanReservationStore {
    override fun find(code: String): BatchedServicePlanReservation? {
        val hit = (cache.get(code) as BatchedServicePlanReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedServicePlanReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedServicePlanReservation> = inner.list()
}

fun newCachedBatchedServicePlanReservationStore(inner: BatchedServicePlanReservationStore, ttl: Int, clock: Clock): BatchedServicePlanReservationStore {
    return CachedBatchedServicePlanReservationStore(
        inner = inner,
        cache = newTtlCache<BatchedServicePlanReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched service plan reservations. Empty fields match all. */
data class BatchedServicePlanReservationQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedServicePlanReservation): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<BatchedServicePlanReservation>): MutableList<BatchedServicePlanReservation> {
        val matched = filter(items) { candidate: BatchedServicePlanReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedServicePlanReservations(store: BatchedServicePlanReservationStore, query: BatchedServicePlanReservationQuery): MutableList<BatchedServicePlanReservation> {
    return query.apply(store.list())
}
