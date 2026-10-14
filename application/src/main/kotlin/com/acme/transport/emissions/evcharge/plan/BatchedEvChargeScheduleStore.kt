package com.acme.transport.emissions.evcharge.plan

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched ev charge schedules by code. */
interface BatchedEvChargeScheduleStore {
    fun find(code: String): BatchedEvChargeSchedule?
    fun save(item: BatchedEvChargeSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedEvChargeSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedEvChargeScheduleStore(
    var items: MutableMap<String, BatchedEvChargeSchedule>,
    var order: MutableList<String>,
) : BatchedEvChargeScheduleStore {
    override fun find(code: String): BatchedEvChargeSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedEvChargeSchedule) {
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

    override fun list(): MutableList<BatchedEvChargeSchedule> {
        val out = mutableListOf<BatchedEvChargeSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedEvChargeScheduleStore(): BatchedEvChargeScheduleStore {
    return MemoryBatchedEvChargeScheduleStore(
        mutableMapOf<String, BatchedEvChargeSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedEvChargeScheduleStore(
    var inner: BatchedEvChargeScheduleStore,
    var cache: TtlCache<BatchedEvChargeSchedule>,
    var hits: Int,
    var misses: Int,
) : BatchedEvChargeScheduleStore {
    override fun find(code: String): BatchedEvChargeSchedule? {
        val hit = (cache.get(code) as BatchedEvChargeSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedEvChargeSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedEvChargeSchedule> = inner.list()
}

fun newCachedBatchedEvChargeScheduleStore(inner: BatchedEvChargeScheduleStore, ttl: Int, clock: Clock): BatchedEvChargeScheduleStore {
    return CachedBatchedEvChargeScheduleStore(
        inner = inner,
        cache = newTtlCache<BatchedEvChargeSchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched ev charge schedules. Empty fields match all. */
data class BatchedEvChargeScheduleQuery(
    var lane: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedEvChargeSchedule): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedEvChargeSchedule>): MutableList<BatchedEvChargeSchedule> {
        val matched = filter(items) { candidate: BatchedEvChargeSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedEvChargeSchedules(store: BatchedEvChargeScheduleStore, query: BatchedEvChargeScheduleQuery): MutableList<BatchedEvChargeSchedule> {
    return query.apply(store.list())
}
