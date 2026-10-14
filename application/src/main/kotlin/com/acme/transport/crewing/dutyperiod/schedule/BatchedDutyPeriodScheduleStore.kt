package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched duty period schedules by code. */
interface BatchedDutyPeriodScheduleStore {
    fun find(code: String): BatchedDutyPeriodSchedule?
    fun save(item: BatchedDutyPeriodSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDutyPeriodSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDutyPeriodScheduleStore(
    var items: MutableMap<String, BatchedDutyPeriodSchedule>,
    var order: MutableList<String>,
) : BatchedDutyPeriodScheduleStore {
    override fun find(code: String): BatchedDutyPeriodSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDutyPeriodSchedule) {
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

    override fun list(): MutableList<BatchedDutyPeriodSchedule> {
        val out = mutableListOf<BatchedDutyPeriodSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDutyPeriodScheduleStore(): BatchedDutyPeriodScheduleStore {
    return MemoryBatchedDutyPeriodScheduleStore(
        mutableMapOf<String, BatchedDutyPeriodSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDutyPeriodScheduleStore(
    var inner: BatchedDutyPeriodScheduleStore,
    var cache: TtlCache<BatchedDutyPeriodSchedule>,
    var hits: Int,
    var misses: Int,
) : BatchedDutyPeriodScheduleStore {
    override fun find(code: String): BatchedDutyPeriodSchedule? {
        val hit = (cache.get(code) as BatchedDutyPeriodSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDutyPeriodSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDutyPeriodSchedule> = inner.list()
}

fun newCachedBatchedDutyPeriodScheduleStore(inner: BatchedDutyPeriodScheduleStore, ttl: Int, clock: Clock): BatchedDutyPeriodScheduleStore {
    return CachedBatchedDutyPeriodScheduleStore(
        inner = inner,
        cache = newTtlCache<BatchedDutyPeriodSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched duty period schedules. Empty fields match all. */
data class BatchedDutyPeriodScheduleQuery(
    var category: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDutyPeriodSchedule): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<BatchedDutyPeriodSchedule>): MutableList<BatchedDutyPeriodSchedule> {
        val matched = filter(items) { candidate: BatchedDutyPeriodSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDutyPeriodSchedules(store: BatchedDutyPeriodScheduleStore, query: BatchedDutyPeriodScheduleQuery): MutableList<BatchedDutyPeriodSchedule> {
    return query.apply(store.list())
}
