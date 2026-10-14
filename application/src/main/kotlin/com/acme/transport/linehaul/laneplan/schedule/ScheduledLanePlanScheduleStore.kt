package com.acme.transport.linehaul.laneplan.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled lane plan schedules by code. */
interface ScheduledLanePlanScheduleStore {
    fun find(code: String): ScheduledLanePlanSchedule?
    fun save(item: ScheduledLanePlanSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLanePlanSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLanePlanScheduleStore(
    var items: MutableMap<String, ScheduledLanePlanSchedule>,
    var order: MutableList<String>,
) : ScheduledLanePlanScheduleStore {
    override fun find(code: String): ScheduledLanePlanSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLanePlanSchedule) {
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

    override fun list(): MutableList<ScheduledLanePlanSchedule> {
        val out = mutableListOf<ScheduledLanePlanSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLanePlanScheduleStore(): ScheduledLanePlanScheduleStore {
    return MemoryScheduledLanePlanScheduleStore(
        mutableMapOf<String, ScheduledLanePlanSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLanePlanScheduleStore(
    var inner: ScheduledLanePlanScheduleStore,
    var cache: TtlCache<ScheduledLanePlanSchedule>,
    var hits: Int,
    var misses: Int,
) : ScheduledLanePlanScheduleStore {
    override fun find(code: String): ScheduledLanePlanSchedule? {
        val hit = (cache.get(code) as ScheduledLanePlanSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLanePlanSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLanePlanSchedule> = inner.list()
}

fun newCachedScheduledLanePlanScheduleStore(inner: ScheduledLanePlanScheduleStore, ttl: Int, clock: Clock): ScheduledLanePlanScheduleStore {
    return CachedScheduledLanePlanScheduleStore(
        inner = inner,
        cache = newTtlCache<ScheduledLanePlanSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled lane plan schedules. Empty fields match all. */
data class ScheduledLanePlanScheduleQuery(
    var locale: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLanePlanSchedule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<ScheduledLanePlanSchedule>): MutableList<ScheduledLanePlanSchedule> {
        val matched = filter(items) { candidate: ScheduledLanePlanSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLanePlanSchedules(store: ScheduledLanePlanScheduleStore, query: ScheduledLanePlanScheduleQuery): MutableList<ScheduledLanePlanSchedule> {
    return query.apply(store.list())
}
