package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled near miss schedules by code. */
interface ScheduledNearMissScheduleStore {
    fun find(code: String): ScheduledNearMissSchedule?
    fun save(item: ScheduledNearMissSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledNearMissSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledNearMissScheduleStore(
    var items: MutableMap<String, ScheduledNearMissSchedule>,
    var order: MutableList<String>,
) : ScheduledNearMissScheduleStore {
    override fun find(code: String): ScheduledNearMissSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledNearMissSchedule) {
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

    override fun list(): MutableList<ScheduledNearMissSchedule> {
        val out = mutableListOf<ScheduledNearMissSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledNearMissScheduleStore(): ScheduledNearMissScheduleStore {
    return MemoryScheduledNearMissScheduleStore(
        mutableMapOf<String, ScheduledNearMissSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledNearMissScheduleStore(
    var inner: ScheduledNearMissScheduleStore,
    var cache: TtlCache<ScheduledNearMissSchedule>,
    var hits: Int,
    var misses: Int,
) : ScheduledNearMissScheduleStore {
    override fun find(code: String): ScheduledNearMissSchedule? {
        val hit = (cache.get(code) as ScheduledNearMissSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledNearMissSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledNearMissSchedule> = inner.list()
}

fun newCachedScheduledNearMissScheduleStore(inner: ScheduledNearMissScheduleStore, ttl: Int, clock: Clock): ScheduledNearMissScheduleStore {
    return CachedScheduledNearMissScheduleStore(
        inner = inner,
        cache = newTtlCache<ScheduledNearMissSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled near miss schedules. Empty fields match all. */
data class ScheduledNearMissScheduleQuery(
    var segment: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledNearMissSchedule): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledNearMissSchedule>): MutableList<ScheduledNearMissSchedule> {
        val matched = filter(items) { candidate: ScheduledNearMissSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledNearMissSchedules(store: ScheduledNearMissScheduleStore, query: ScheduledNearMissScheduleQuery): MutableList<ScheduledNearMissSchedule> {
    return query.apply(store.list())
}
