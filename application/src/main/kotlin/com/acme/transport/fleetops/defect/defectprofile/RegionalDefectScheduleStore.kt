package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional defect schedules by code. */
interface RegionalDefectScheduleStore {
    fun find(code: String): RegionalDefectSchedule?
    fun save(item: RegionalDefectSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDefectSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDefectScheduleStore(
    var items: MutableMap<String, RegionalDefectSchedule>,
    var order: MutableList<String>,
) : RegionalDefectScheduleStore {
    override fun find(code: String): RegionalDefectSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDefectSchedule) {
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

    override fun list(): MutableList<RegionalDefectSchedule> {
        val out = mutableListOf<RegionalDefectSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDefectScheduleStore(): RegionalDefectScheduleStore {
    return MemoryRegionalDefectScheduleStore(
        mutableMapOf<String, RegionalDefectSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDefectScheduleStore(
    var inner: RegionalDefectScheduleStore,
    var cache: TtlCache<RegionalDefectSchedule>,
    var hits: Int,
    var misses: Int,
) : RegionalDefectScheduleStore {
    override fun find(code: String): RegionalDefectSchedule? {
        val hit = (cache.get(code) as RegionalDefectSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDefectSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDefectSchedule> = inner.list()
}

fun newCachedRegionalDefectScheduleStore(inner: RegionalDefectScheduleStore, ttl: Int, clock: Clock): RegionalDefectScheduleStore {
    return CachedRegionalDefectScheduleStore(
        inner = inner,
        cache = newTtlCache<RegionalDefectSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional defect schedules. Empty fields match all. */
data class RegionalDefectScheduleQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDefectSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDefectSchedule>): MutableList<RegionalDefectSchedule> {
        val matched = filter(items) { candidate: RegionalDefectSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDefectSchedules(store: RegionalDefectScheduleStore, query: RegionalDefectScheduleQuery): MutableList<RegionalDefectSchedule> {
    return query.apply(store.list())
}
