package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched capacity schedules by code. */
interface BatchedCapacityScheduleStore {
    fun find(code: String): BatchedCapacitySchedule?
    fun save(item: BatchedCapacitySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCapacitySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCapacityScheduleStore(
    var items: MutableMap<String, BatchedCapacitySchedule>,
    var order: MutableList<String>,
) : BatchedCapacityScheduleStore {
    override fun find(code: String): BatchedCapacitySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCapacitySchedule) {
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

    override fun list(): MutableList<BatchedCapacitySchedule> {
        val out = mutableListOf<BatchedCapacitySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCapacityScheduleStore(): BatchedCapacityScheduleStore {
    return MemoryBatchedCapacityScheduleStore(
        mutableMapOf<String, BatchedCapacitySchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCapacityScheduleStore(
    var inner: BatchedCapacityScheduleStore,
    var cache: TtlCache<BatchedCapacitySchedule>,
    var hits: Int,
    var misses: Int,
) : BatchedCapacityScheduleStore {
    override fun find(code: String): BatchedCapacitySchedule? {
        val hit = (cache.get(code) as BatchedCapacitySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCapacitySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCapacitySchedule> = inner.list()
}

fun newCachedBatchedCapacityScheduleStore(inner: BatchedCapacityScheduleStore, ttl: Int, clock: Clock): BatchedCapacityScheduleStore {
    return CachedBatchedCapacityScheduleStore(
        inner = inner,
        cache = newTtlCache<BatchedCapacitySchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched capacity schedules. Empty fields match all. */
data class BatchedCapacityScheduleQuery(
    var lane: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCapacitySchedule): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedCapacitySchedule>): MutableList<BatchedCapacitySchedule> {
        val matched = filter(items) { candidate: BatchedCapacitySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCapacitySchedules(store: BatchedCapacityScheduleStore, query: BatchedCapacityScheduleQuery): MutableList<BatchedCapacitySchedule> {
    return query.apply(store.list())
}
