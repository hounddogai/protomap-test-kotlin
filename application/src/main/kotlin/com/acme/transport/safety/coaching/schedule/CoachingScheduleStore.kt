package com.acme.transport.safety.coaching.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching schedules by code. */
interface CoachingScheduleStore {
    fun find(code: String): CoachingSchedule?
    fun save(item: CoachingSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingScheduleStore(
    var items: MutableMap<String, CoachingSchedule>,
    var order: MutableList<String>,
) : CoachingScheduleStore {
    override fun find(code: String): CoachingSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingSchedule) {
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

    override fun list(): MutableList<CoachingSchedule> {
        val out = mutableListOf<CoachingSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingScheduleStore(): CoachingScheduleStore {
    return MemoryCoachingScheduleStore(mutableMapOf<String, CoachingSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingScheduleStore(
    var inner: CoachingScheduleStore,
    var cache: TtlCache<CoachingSchedule>,
    var hits: Int,
    var misses: Int,
) : CoachingScheduleStore {
    override fun find(code: String): CoachingSchedule? {
        val hit = (cache.get(code) as CoachingSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingSchedule> = inner.list()
}

fun newCachedCoachingScheduleStore(inner: CoachingScheduleStore, ttl: Int, clock: Clock): CoachingScheduleStore {
    return CachedCoachingScheduleStore(
        inner = inner,
        cache = newTtlCache<CoachingSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching schedules. Empty fields match all. */
data class CoachingScheduleQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingSchedule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingSchedule>): MutableList<CoachingSchedule> {
        val matched = filter(items) { candidate: CoachingSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingSchedules(store: CoachingScheduleStore, query: CoachingScheduleQuery): MutableList<CoachingSchedule> {
    return query.apply(store.list())
}
