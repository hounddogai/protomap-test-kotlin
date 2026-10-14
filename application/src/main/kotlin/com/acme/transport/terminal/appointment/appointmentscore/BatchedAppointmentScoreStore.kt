package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched appointment scores by code. */
interface BatchedAppointmentScoreStore {
    fun find(code: String): BatchedAppointmentScore?
    fun save(item: BatchedAppointmentScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAppointmentScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAppointmentScoreStore(
    var items: MutableMap<String, BatchedAppointmentScore>,
    var order: MutableList<String>,
) : BatchedAppointmentScoreStore {
    override fun find(code: String): BatchedAppointmentScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAppointmentScore) {
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

    override fun list(): MutableList<BatchedAppointmentScore> {
        val out = mutableListOf<BatchedAppointmentScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAppointmentScoreStore(): BatchedAppointmentScoreStore {
    return MemoryBatchedAppointmentScoreStore(
        mutableMapOf<String, BatchedAppointmentScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAppointmentScoreStore(
    var inner: BatchedAppointmentScoreStore,
    var cache: TtlCache<BatchedAppointmentScore>,
    var hits: Int,
    var misses: Int,
) : BatchedAppointmentScoreStore {
    override fun find(code: String): BatchedAppointmentScore? {
        val hit = (cache.get(code) as BatchedAppointmentScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAppointmentScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAppointmentScore> = inner.list()
}

fun newCachedBatchedAppointmentScoreStore(inner: BatchedAppointmentScoreStore, ttl: Int, clock: Clock): BatchedAppointmentScoreStore {
    return CachedBatchedAppointmentScoreStore(
        inner = inner,
        cache = newTtlCache<BatchedAppointmentScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched appointment scores. Empty fields match all. */
data class BatchedAppointmentScoreQuery(
    var channel: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAppointmentScore): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAppointmentScore>): MutableList<BatchedAppointmentScore> {
        val matched = filter(items) { candidate: BatchedAppointmentScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAppointmentScores(store: BatchedAppointmentScoreStore, query: BatchedAppointmentScoreQuery): MutableList<BatchedAppointmentScore> {
    return query.apply(store.list())
}
