package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal duty period schedules by code. */
interface SeasonalDutyPeriodScheduleStore {
    fun find(code: String): SeasonalDutyPeriodSchedule?
    fun save(item: SeasonalDutyPeriodSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDutyPeriodSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDutyPeriodScheduleStore(
    var items: MutableMap<String, SeasonalDutyPeriodSchedule>,
    var order: MutableList<String>,
) : SeasonalDutyPeriodScheduleStore {
    override fun find(code: String): SeasonalDutyPeriodSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDutyPeriodSchedule) {
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

    override fun list(): MutableList<SeasonalDutyPeriodSchedule> {
        val out = mutableListOf<SeasonalDutyPeriodSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDutyPeriodScheduleStore(): SeasonalDutyPeriodScheduleStore {
    return MemorySeasonalDutyPeriodScheduleStore(
        mutableMapOf<String, SeasonalDutyPeriodSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDutyPeriodScheduleStore(
    var inner: SeasonalDutyPeriodScheduleStore,
    var cache: TtlCache<SeasonalDutyPeriodSchedule>,
    var hits: Int,
    var misses: Int,
) : SeasonalDutyPeriodScheduleStore {
    override fun find(code: String): SeasonalDutyPeriodSchedule? {
        val hit = (cache.get(code) as SeasonalDutyPeriodSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDutyPeriodSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDutyPeriodSchedule> = inner.list()
}

fun newCachedSeasonalDutyPeriodScheduleStore(inner: SeasonalDutyPeriodScheduleStore, ttl: Int, clock: Clock): SeasonalDutyPeriodScheduleStore {
    return CachedSeasonalDutyPeriodScheduleStore(
        inner = inner,
        cache = newTtlCache<SeasonalDutyPeriodSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal duty period schedules. Empty fields match all. */
data class SeasonalDutyPeriodScheduleQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDutyPeriodSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<SeasonalDutyPeriodSchedule>): MutableList<SeasonalDutyPeriodSchedule> {
        val matched = filter(items) { candidate: SeasonalDutyPeriodSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDutyPeriodSchedules(store: SeasonalDutyPeriodScheduleStore, query: SeasonalDutyPeriodScheduleQuery): MutableList<SeasonalDutyPeriodSchedule> {
    return query.apply(store.list())
}
