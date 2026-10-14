package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard schedules by code. */
interface ScorecardScheduleStore {
    fun find(code: String): ScorecardSchedule?
    fun save(item: ScorecardSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardScheduleStore(
    var items: MutableMap<String, ScorecardSchedule>,
    var order: MutableList<String>,
) : ScorecardScheduleStore {
    override fun find(code: String): ScorecardSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardSchedule) {
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

    override fun list(): MutableList<ScorecardSchedule> {
        val out = mutableListOf<ScorecardSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardScheduleStore(): ScorecardScheduleStore {
    return MemoryScorecardScheduleStore(mutableMapOf<String, ScorecardSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardScheduleStore(
    var inner: ScorecardScheduleStore,
    var cache: TtlCache<ScorecardSchedule>,
    var hits: Int,
    var misses: Int,
) : ScorecardScheduleStore {
    override fun find(code: String): ScorecardSchedule? {
        val hit = (cache.get(code) as ScorecardSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardSchedule> = inner.list()
}

fun newCachedScorecardScheduleStore(inner: ScorecardScheduleStore, ttl: Int, clock: Clock): ScorecardScheduleStore {
    return CachedScorecardScheduleStore(
        inner = inner,
        cache = newTtlCache<ScorecardSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard schedules. Empty fields match all. */
data class ScorecardScheduleQuery(
    var country: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardSchedule): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardSchedule>): MutableList<ScorecardSchedule> {
        val matched = filter(items) { candidate: ScorecardSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardSchedules(store: ScorecardScheduleStore, query: ScorecardScheduleQuery): MutableList<ScorecardSchedule> {
    return query.apply(store.list())
}
