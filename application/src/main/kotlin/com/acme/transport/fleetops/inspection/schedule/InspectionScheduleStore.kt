package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection schedules by code. */
interface InspectionScheduleStore {
    fun find(code: String): InspectionSchedule?
    fun save(item: InspectionSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionScheduleStore(
    var items: MutableMap<String, InspectionSchedule>,
    var order: MutableList<String>,
) : InspectionScheduleStore {
    override fun find(code: String): InspectionSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionSchedule) {
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

    override fun list(): MutableList<InspectionSchedule> {
        val out = mutableListOf<InspectionSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionScheduleStore(): InspectionScheduleStore {
    return MemoryInspectionScheduleStore(mutableMapOf<String, InspectionSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionScheduleStore(
    var inner: InspectionScheduleStore,
    var cache: TtlCache<InspectionSchedule>,
    var hits: Int,
    var misses: Int,
) : InspectionScheduleStore {
    override fun find(code: String): InspectionSchedule? {
        val hit = (cache.get(code) as InspectionSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionSchedule> = inner.list()
}

fun newCachedInspectionScheduleStore(inner: InspectionScheduleStore, ttl: Int, clock: Clock): InspectionScheduleStore {
    return CachedInspectionScheduleStore(
        inner = inner,
        cache = newTtlCache<InspectionSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection schedules. Empty fields match all. */
data class InspectionScheduleQuery(
    var source: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionSchedule): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionSchedule>): MutableList<InspectionSchedule> {
        val matched = filter(items) { candidate: InspectionSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionSchedules(store: InspectionScheduleStore, query: InspectionScheduleQuery): MutableList<InspectionSchedule> {
    return query.apply(store.list())
}
