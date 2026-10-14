package com.acme.transport.network.cutoff.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched cutoff reservations by code. */
interface BatchedCutoffReservationStore {
    fun find(code: String): BatchedCutoffReservation?
    fun save(item: BatchedCutoffReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCutoffReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCutoffReservationStore(
    var items: MutableMap<String, BatchedCutoffReservation>,
    var order: MutableList<String>,
) : BatchedCutoffReservationStore {
    override fun find(code: String): BatchedCutoffReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCutoffReservation) {
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

    override fun list(): MutableList<BatchedCutoffReservation> {
        val out = mutableListOf<BatchedCutoffReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCutoffReservationStore(): BatchedCutoffReservationStore {
    return MemoryBatchedCutoffReservationStore(
        mutableMapOf<String, BatchedCutoffReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCutoffReservationStore(
    var inner: BatchedCutoffReservationStore,
    var cache: TtlCache<BatchedCutoffReservation>,
    var hits: Int,
    var misses: Int,
) : BatchedCutoffReservationStore {
    override fun find(code: String): BatchedCutoffReservation? {
        val hit = (cache.get(code) as BatchedCutoffReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCutoffReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCutoffReservation> = inner.list()
}

fun newCachedBatchedCutoffReservationStore(inner: BatchedCutoffReservationStore, ttl: Int, clock: Clock): BatchedCutoffReservationStore {
    return CachedBatchedCutoffReservationStore(
        inner = inner,
        cache = newTtlCache<BatchedCutoffReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched cutoff reservations. Empty fields match all. */
data class BatchedCutoffReservationQuery(
    var channel: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCutoffReservation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedCutoffReservation>): MutableList<BatchedCutoffReservation> {
        val matched = filter(items) { candidate: BatchedCutoffReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCutoffReservations(store: BatchedCutoffReservationStore, query: BatchedCutoffReservationQuery): MutableList<BatchedCutoffReservation> {
    return query.apply(store.list())
}
