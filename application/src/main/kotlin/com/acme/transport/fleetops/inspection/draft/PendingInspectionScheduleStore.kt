package com.acme.transport.fleetops.inspection.draft

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending inspection schedules by code. */
interface PendingInspectionScheduleStore {
    fun find(code: String): PendingInspectionSchedule?
    fun save(item: PendingInspectionSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingInspectionSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingInspectionScheduleStore(
    var items: MutableMap<String, PendingInspectionSchedule>,
    var order: MutableList<String>,
) : PendingInspectionScheduleStore {
    override fun find(code: String): PendingInspectionSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingInspectionSchedule) {
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

    override fun list(): MutableList<PendingInspectionSchedule> {
        val out = mutableListOf<PendingInspectionSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingInspectionScheduleStore(): PendingInspectionScheduleStore {
    return MemoryPendingInspectionScheduleStore(
        mutableMapOf<String, PendingInspectionSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingInspectionScheduleStore(
    var inner: PendingInspectionScheduleStore,
    var cache: TtlCache<PendingInspectionSchedule>,
    var hits: Int,
    var misses: Int,
) : PendingInspectionScheduleStore {
    override fun find(code: String): PendingInspectionSchedule? {
        val hit = (cache.get(code) as PendingInspectionSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingInspectionSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingInspectionSchedule> = inner.list()
}

fun newCachedPendingInspectionScheduleStore(inner: PendingInspectionScheduleStore, ttl: Int, clock: Clock): PendingInspectionScheduleStore {
    return CachedPendingInspectionScheduleStore(
        inner = inner,
        cache = newTtlCache<PendingInspectionSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending inspection schedules. Empty fields match all. */
data class PendingInspectionScheduleQuery(
    var depot: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingInspectionSchedule): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<PendingInspectionSchedule>): MutableList<PendingInspectionSchedule> {
        val matched = filter(items) { candidate: PendingInspectionSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingInspectionSchedules(store: PendingInspectionScheduleStore, query: PendingInspectionScheduleQuery): MutableList<PendingInspectionSchedule> {
    return query.apply(store.list())
}
