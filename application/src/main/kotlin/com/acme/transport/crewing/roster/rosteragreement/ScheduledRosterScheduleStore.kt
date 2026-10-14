package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled roster schedules by code. */
interface ScheduledRosterScheduleStore {
    fun find(code: String): ScheduledRosterSchedule?
    fun save(item: ScheduledRosterSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRosterSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRosterScheduleStore(
    var items: MutableMap<String, ScheduledRosterSchedule>,
    var order: MutableList<String>,
) : ScheduledRosterScheduleStore {
    override fun find(code: String): ScheduledRosterSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRosterSchedule) {
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

    override fun list(): MutableList<ScheduledRosterSchedule> {
        val out = mutableListOf<ScheduledRosterSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRosterScheduleStore(): ScheduledRosterScheduleStore {
    return MemoryScheduledRosterScheduleStore(
        mutableMapOf<String, ScheduledRosterSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRosterScheduleStore(
    var inner: ScheduledRosterScheduleStore,
    var cache: TtlCache<ScheduledRosterSchedule>,
    var hits: Int,
    var misses: Int,
) : ScheduledRosterScheduleStore {
    override fun find(code: String): ScheduledRosterSchedule? {
        val hit = (cache.get(code) as ScheduledRosterSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRosterSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRosterSchedule> = inner.list()
}

fun newCachedScheduledRosterScheduleStore(inner: ScheduledRosterScheduleStore, ttl: Int, clock: Clock): ScheduledRosterScheduleStore {
    return CachedScheduledRosterScheduleStore(
        inner = inner,
        cache = newTtlCache<ScheduledRosterSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled roster schedules. Empty fields match all. */
data class ScheduledRosterScheduleQuery(
    var unit: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRosterSchedule): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRosterSchedule>): MutableList<ScheduledRosterSchedule> {
        val matched = filter(items) { candidate: ScheduledRosterSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRosterSchedules(store: ScheduledRosterScheduleStore, query: ScheduledRosterScheduleQuery): MutableList<ScheduledRosterSchedule> {
    return query.apply(store.list())
}
