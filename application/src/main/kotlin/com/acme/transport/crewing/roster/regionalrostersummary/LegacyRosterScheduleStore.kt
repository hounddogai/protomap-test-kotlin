package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy roster schedules by code. */
interface LegacyRosterScheduleStore {
    fun find(code: String): LegacyRosterSchedule?
    fun save(item: LegacyRosterSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRosterSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRosterScheduleStore(
    var items: MutableMap<String, LegacyRosterSchedule>,
    var order: MutableList<String>,
) : LegacyRosterScheduleStore {
    override fun find(code: String): LegacyRosterSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRosterSchedule) {
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

    override fun list(): MutableList<LegacyRosterSchedule> {
        val out = mutableListOf<LegacyRosterSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRosterScheduleStore(): LegacyRosterScheduleStore {
    return MemoryLegacyRosterScheduleStore(
        mutableMapOf<String, LegacyRosterSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRosterScheduleStore(
    var inner: LegacyRosterScheduleStore,
    var cache: TtlCache<LegacyRosterSchedule>,
    var hits: Int,
    var misses: Int,
) : LegacyRosterScheduleStore {
    override fun find(code: String): LegacyRosterSchedule? {
        val hit = (cache.get(code) as LegacyRosterSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRosterSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRosterSchedule> = inner.list()
}

fun newCachedLegacyRosterScheduleStore(inner: LegacyRosterScheduleStore, ttl: Int, clock: Clock): LegacyRosterScheduleStore {
    return CachedLegacyRosterScheduleStore(
        inner = inner,
        cache = newTtlCache<LegacyRosterSchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy roster schedules. Empty fields match all. */
data class LegacyRosterScheduleQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRosterSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyRosterSchedule>): MutableList<LegacyRosterSchedule> {
        val matched = filter(items) { candidate: LegacyRosterSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRosterSchedules(store: LegacyRosterScheduleStore, query: LegacyRosterScheduleQuery): MutableList<LegacyRosterSchedule> {
    return query.apply(store.list())
}
