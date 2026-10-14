package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect schedules by code. */
interface DefectScheduleStore {
    fun find(code: String): DefectSchedule?
    fun save(item: DefectSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectScheduleStore(
    var items: MutableMap<String, DefectSchedule>,
    var order: MutableList<String>,
) : DefectScheduleStore {
    override fun find(code: String): DefectSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectSchedule) {
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

    override fun list(): MutableList<DefectSchedule> {
        val out = mutableListOf<DefectSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectScheduleStore(): DefectScheduleStore {
    return MemoryDefectScheduleStore(mutableMapOf<String, DefectSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectScheduleStore(
    var inner: DefectScheduleStore,
    var cache: TtlCache<DefectSchedule>,
    var hits: Int,
    var misses: Int,
) : DefectScheduleStore {
    override fun find(code: String): DefectSchedule? {
        val hit = (cache.get(code) as DefectSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectSchedule> = inner.list()
}

fun newCachedDefectScheduleStore(inner: DefectScheduleStore, ttl: Int, clock: Clock): DefectScheduleStore {
    return CachedDefectScheduleStore(
        inner = inner,
        cache = newTtlCache<DefectSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect schedules. Empty fields match all. */
data class DefectScheduleQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectSchedule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectSchedule>): MutableList<DefectSchedule> {
        val matched = filter(items) { candidate: DefectSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectSchedules(store: DefectScheduleStore, query: DefectScheduleQuery): MutableList<DefectSchedule> {
    return query.apply(store.list())
}
