package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default inspection schedules by code. */
interface DefaultInspectionScheduleStore {
    fun find(code: String): DefaultInspectionSchedule?
    fun save(item: DefaultInspectionSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultInspectionSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultInspectionScheduleStore(
    var items: MutableMap<String, DefaultInspectionSchedule>,
    var order: MutableList<String>,
) : DefaultInspectionScheduleStore {
    override fun find(code: String): DefaultInspectionSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultInspectionSchedule) {
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

    override fun list(): MutableList<DefaultInspectionSchedule> {
        val out = mutableListOf<DefaultInspectionSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultInspectionScheduleStore(): DefaultInspectionScheduleStore {
    return MemoryDefaultInspectionScheduleStore(
        mutableMapOf<String, DefaultInspectionSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultInspectionScheduleStore(
    var inner: DefaultInspectionScheduleStore,
    var cache: TtlCache<DefaultInspectionSchedule>,
    var hits: Int,
    var misses: Int,
) : DefaultInspectionScheduleStore {
    override fun find(code: String): DefaultInspectionSchedule? {
        val hit = (cache.get(code) as DefaultInspectionSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultInspectionSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultInspectionSchedule> = inner.list()
}

fun newCachedDefaultInspectionScheduleStore(inner: DefaultInspectionScheduleStore, ttl: Int, clock: Clock): DefaultInspectionScheduleStore {
    return CachedDefaultInspectionScheduleStore(
        inner = inner,
        cache = newTtlCache<DefaultInspectionSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default inspection schedules. Empty fields match all. */
data class DefaultInspectionScheduleQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultInspectionSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultInspectionSchedule>): MutableList<DefaultInspectionSchedule> {
        val matched = filter(items) { candidate: DefaultInspectionSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultInspectionSchedules(store: DefaultInspectionScheduleStore, query: DefaultInspectionScheduleQuery): MutableList<DefaultInspectionSchedule> {
    return query.apply(store.list())
}
