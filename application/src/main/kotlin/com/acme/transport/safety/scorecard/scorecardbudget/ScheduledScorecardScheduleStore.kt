package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled scorecard schedules by code. */
interface ScheduledScorecardScheduleStore {
    fun find(code: String): ScheduledScorecardSchedule?
    fun save(item: ScheduledScorecardSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledScorecardSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledScorecardScheduleStore(
    var items: MutableMap<String, ScheduledScorecardSchedule>,
    var order: MutableList<String>,
) : ScheduledScorecardScheduleStore {
    override fun find(code: String): ScheduledScorecardSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledScorecardSchedule) {
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

    override fun list(): MutableList<ScheduledScorecardSchedule> {
        val out = mutableListOf<ScheduledScorecardSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledScorecardScheduleStore(): ScheduledScorecardScheduleStore {
    return MemoryScheduledScorecardScheduleStore(
        mutableMapOf<String, ScheduledScorecardSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledScorecardScheduleStore(
    var inner: ScheduledScorecardScheduleStore,
    var cache: TtlCache<ScheduledScorecardSchedule>,
    var hits: Int,
    var misses: Int,
) : ScheduledScorecardScheduleStore {
    override fun find(code: String): ScheduledScorecardSchedule? {
        val hit = (cache.get(code) as ScheduledScorecardSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledScorecardSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledScorecardSchedule> = inner.list()
}

fun newCachedScheduledScorecardScheduleStore(inner: ScheduledScorecardScheduleStore, ttl: Int, clock: Clock): ScheduledScorecardScheduleStore {
    return CachedScheduledScorecardScheduleStore(
        inner = inner,
        cache = newTtlCache<ScheduledScorecardSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled scorecard schedules. Empty fields match all. */
data class ScheduledScorecardScheduleQuery(
    var source: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledScorecardSchedule): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<ScheduledScorecardSchedule>): MutableList<ScheduledScorecardSchedule> {
        val matched = filter(items) { candidate: ScheduledScorecardSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledScorecardSchedules(store: ScheduledScorecardScheduleStore, query: ScheduledScorecardScheduleQuery): MutableList<ScheduledScorecardSchedule> {
    return query.apply(store.list())
}
