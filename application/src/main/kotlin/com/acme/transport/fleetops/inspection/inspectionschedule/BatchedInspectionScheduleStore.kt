package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched inspection schedules by code. */
interface BatchedInspectionScheduleStore {
    fun find(code: String): BatchedInspectionSchedule?
    fun save(item: BatchedInspectionSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedInspectionSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedInspectionScheduleStore(
    var items: MutableMap<String, BatchedInspectionSchedule>,
    var order: MutableList<String>,
) : BatchedInspectionScheduleStore {
    override fun find(code: String): BatchedInspectionSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedInspectionSchedule) {
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

    override fun list(): MutableList<BatchedInspectionSchedule> {
        val out = mutableListOf<BatchedInspectionSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedInspectionScheduleStore(): BatchedInspectionScheduleStore {
    return MemoryBatchedInspectionScheduleStore(
        mutableMapOf<String, BatchedInspectionSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedInspectionScheduleStore(
    var inner: BatchedInspectionScheduleStore,
    var cache: TtlCache<BatchedInspectionSchedule>,
    var hits: Int,
    var misses: Int,
) : BatchedInspectionScheduleStore {
    override fun find(code: String): BatchedInspectionSchedule? {
        val hit = (cache.get(code) as BatchedInspectionSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedInspectionSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedInspectionSchedule> = inner.list()
}

fun newCachedBatchedInspectionScheduleStore(inner: BatchedInspectionScheduleStore, ttl: Int, clock: Clock): BatchedInspectionScheduleStore {
    return CachedBatchedInspectionScheduleStore(
        inner = inner,
        cache = newTtlCache<BatchedInspectionSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched inspection schedules. Empty fields match all. */
data class BatchedInspectionScheduleQuery(
    var country: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedInspectionSchedule): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedInspectionSchedule>): MutableList<BatchedInspectionSchedule> {
        val matched = filter(items) { candidate: BatchedInspectionSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedInspectionSchedules(store: BatchedInspectionScheduleStore, query: BatchedInspectionScheduleQuery): MutableList<BatchedInspectionSchedule> {
    return query.apply(store.list())
}
