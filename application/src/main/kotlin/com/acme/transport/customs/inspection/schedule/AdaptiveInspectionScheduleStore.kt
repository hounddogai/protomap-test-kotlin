package com.acme.transport.customs.inspection.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive inspection schedules by code. */
interface AdaptiveInspectionScheduleStore {
    fun find(code: String): AdaptiveInspectionSchedule?
    fun save(item: AdaptiveInspectionSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveInspectionSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveInspectionScheduleStore(
    var items: MutableMap<String, AdaptiveInspectionSchedule>,
    var order: MutableList<String>,
) : AdaptiveInspectionScheduleStore {
    override fun find(code: String): AdaptiveInspectionSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveInspectionSchedule) {
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

    override fun list(): MutableList<AdaptiveInspectionSchedule> {
        val out = mutableListOf<AdaptiveInspectionSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveInspectionScheduleStore(): AdaptiveInspectionScheduleStore {
    return MemoryAdaptiveInspectionScheduleStore(
        mutableMapOf<String, AdaptiveInspectionSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveInspectionScheduleStore(
    var inner: AdaptiveInspectionScheduleStore,
    var cache: TtlCache<AdaptiveInspectionSchedule>,
    var hits: Int,
    var misses: Int,
) : AdaptiveInspectionScheduleStore {
    override fun find(code: String): AdaptiveInspectionSchedule? {
        val hit = (cache.get(code) as AdaptiveInspectionSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveInspectionSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveInspectionSchedule> = inner.list()
}

fun newCachedAdaptiveInspectionScheduleStore(inner: AdaptiveInspectionScheduleStore, ttl: Int, clock: Clock): AdaptiveInspectionScheduleStore {
    return CachedAdaptiveInspectionScheduleStore(
        inner = inner,
        cache = newTtlCache<AdaptiveInspectionSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive inspection schedules. Empty fields match all. */
data class AdaptiveInspectionScheduleQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveInspectionSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveInspectionSchedule>): MutableList<AdaptiveInspectionSchedule> {
        val matched = filter(items) { candidate: AdaptiveInspectionSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveInspectionSchedules(store: AdaptiveInspectionScheduleStore, query: AdaptiveInspectionScheduleQuery): MutableList<AdaptiveInspectionSchedule> {
    return query.apply(store.list())
}
