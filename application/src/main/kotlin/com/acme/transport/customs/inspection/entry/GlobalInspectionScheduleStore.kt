package com.acme.transport.customs.inspection.entry

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global inspection schedules by code. */
interface GlobalInspectionScheduleStore {
    fun find(code: String): GlobalInspectionSchedule?
    fun save(item: GlobalInspectionSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalInspectionSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalInspectionScheduleStore(
    var items: MutableMap<String, GlobalInspectionSchedule>,
    var order: MutableList<String>,
) : GlobalInspectionScheduleStore {
    override fun find(code: String): GlobalInspectionSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalInspectionSchedule) {
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

    override fun list(): MutableList<GlobalInspectionSchedule> {
        val out = mutableListOf<GlobalInspectionSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalInspectionScheduleStore(): GlobalInspectionScheduleStore {
    return MemoryGlobalInspectionScheduleStore(
        mutableMapOf<String, GlobalInspectionSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalInspectionScheduleStore(
    var inner: GlobalInspectionScheduleStore,
    var cache: TtlCache<GlobalInspectionSchedule>,
    var hits: Int,
    var misses: Int,
) : GlobalInspectionScheduleStore {
    override fun find(code: String): GlobalInspectionSchedule? {
        val hit = (cache.get(code) as GlobalInspectionSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalInspectionSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalInspectionSchedule> = inner.list()
}

fun newCachedGlobalInspectionScheduleStore(inner: GlobalInspectionScheduleStore, ttl: Int, clock: Clock): GlobalInspectionScheduleStore {
    return CachedGlobalInspectionScheduleStore(
        inner = inner,
        cache = newTtlCache<GlobalInspectionSchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global inspection schedules. Empty fields match all. */
data class GlobalInspectionScheduleQuery(
    var depot: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalInspectionSchedule): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalInspectionSchedule>): MutableList<GlobalInspectionSchedule> {
        val matched = filter(items) { candidate: GlobalInspectionSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalInspectionSchedules(store: GlobalInspectionScheduleStore, query: GlobalInspectionScheduleQuery): MutableList<GlobalInspectionSchedule> {
    return query.apply(store.list())
}
