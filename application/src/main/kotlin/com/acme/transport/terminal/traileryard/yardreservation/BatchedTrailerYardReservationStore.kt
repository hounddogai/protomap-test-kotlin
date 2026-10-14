package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched trailer yard reservations by code. */
interface BatchedTrailerYardReservationStore {
    fun find(code: String): BatchedTrailerYardReservation?
    fun save(item: BatchedTrailerYardReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTrailerYardReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTrailerYardReservationStore(
    var items: MutableMap<String, BatchedTrailerYardReservation>,
    var order: MutableList<String>,
) : BatchedTrailerYardReservationStore {
    override fun find(code: String): BatchedTrailerYardReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTrailerYardReservation) {
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

    override fun list(): MutableList<BatchedTrailerYardReservation> {
        val out = mutableListOf<BatchedTrailerYardReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTrailerYardReservationStore(): BatchedTrailerYardReservationStore {
    return MemoryBatchedTrailerYardReservationStore(
        mutableMapOf<String, BatchedTrailerYardReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTrailerYardReservationStore(
    var inner: BatchedTrailerYardReservationStore,
    var cache: TtlCache<BatchedTrailerYardReservation>,
    var hits: Int,
    var misses: Int,
) : BatchedTrailerYardReservationStore {
    override fun find(code: String): BatchedTrailerYardReservation? {
        val hit = (cache.get(code) as BatchedTrailerYardReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTrailerYardReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTrailerYardReservation> = inner.list()
}

fun newCachedBatchedTrailerYardReservationStore(inner: BatchedTrailerYardReservationStore, ttl: Int, clock: Clock): BatchedTrailerYardReservationStore {
    return CachedBatchedTrailerYardReservationStore(
        inner = inner,
        cache = newTtlCache<BatchedTrailerYardReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched trailer yard reservations. Empty fields match all. */
data class BatchedTrailerYardReservationQuery(
    var channel: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTrailerYardReservation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedTrailerYardReservation>): MutableList<BatchedTrailerYardReservation> {
        val matched = filter(items) { candidate: BatchedTrailerYardReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTrailerYardReservations(store: BatchedTrailerYardReservationStore, query: BatchedTrailerYardReservationQuery): MutableList<BatchedTrailerYardReservation> {
    return query.apply(store.list())
}
