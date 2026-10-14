package com.acme.transport.crewing.roster.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster schedules by code. */
interface RosterScheduleStore {
    fun find(code: String): RosterSchedule?
    fun save(item: RosterSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterScheduleStore(
    var items: MutableMap<String, RosterSchedule>,
    var order: MutableList<String>,
) : RosterScheduleStore {
    override fun find(code: String): RosterSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterSchedule) {
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

    override fun list(): MutableList<RosterSchedule> {
        val out = mutableListOf<RosterSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterScheduleStore(): RosterScheduleStore {
    return MemoryRosterScheduleStore(mutableMapOf<String, RosterSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterScheduleStore(
    var inner: RosterScheduleStore,
    var cache: TtlCache<RosterSchedule>,
    var hits: Int,
    var misses: Int,
) : RosterScheduleStore {
    override fun find(code: String): RosterSchedule? {
        val hit = (cache.get(code) as RosterSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterSchedule> = inner.list()
}

fun newCachedRosterScheduleStore(inner: RosterScheduleStore, ttl: Int, clock: Clock): RosterScheduleStore {
    return CachedRosterScheduleStore(
        inner = inner,
        cache = newTtlCache<RosterSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster schedules. Empty fields match all. */
data class RosterScheduleQuery(
    var locale: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterSchedule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<RosterSchedule>): MutableList<RosterSchedule> {
        val matched = filter(items) { candidate: RosterSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterSchedules(store: RosterScheduleStore, query: RosterScheduleQuery): MutableList<RosterSchedule> {
    return query.apply(store.list())
}
