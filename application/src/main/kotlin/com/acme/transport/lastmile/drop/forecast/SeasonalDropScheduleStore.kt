package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal drop schedules by code. */
interface SeasonalDropScheduleStore {
    fun find(code: String): SeasonalDropSchedule?
    fun save(item: SeasonalDropSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDropSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDropScheduleStore(
    var items: MutableMap<String, SeasonalDropSchedule>,
    var order: MutableList<String>,
) : SeasonalDropScheduleStore {
    override fun find(code: String): SeasonalDropSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDropSchedule) {
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

    override fun list(): MutableList<SeasonalDropSchedule> {
        val out = mutableListOf<SeasonalDropSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDropScheduleStore(): SeasonalDropScheduleStore {
    return MemorySeasonalDropScheduleStore(
        mutableMapOf<String, SeasonalDropSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDropScheduleStore(
    var inner: SeasonalDropScheduleStore,
    var cache: TtlCache<SeasonalDropSchedule>,
    var hits: Int,
    var misses: Int,
) : SeasonalDropScheduleStore {
    override fun find(code: String): SeasonalDropSchedule? {
        val hit = (cache.get(code) as SeasonalDropSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDropSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDropSchedule> = inner.list()
}

fun newCachedSeasonalDropScheduleStore(inner: SeasonalDropScheduleStore, ttl: Int, clock: Clock): SeasonalDropScheduleStore {
    return CachedSeasonalDropScheduleStore(
        inner = inner,
        cache = newTtlCache<SeasonalDropSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal drop schedules. Empty fields match all. */
data class SeasonalDropScheduleQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDropSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDropSchedule>): MutableList<SeasonalDropSchedule> {
        val matched = filter(items) { candidate: SeasonalDropSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDropSchedules(store: SeasonalDropScheduleStore, query: SeasonalDropScheduleQuery): MutableList<SeasonalDropSchedule> {
    return query.apply(store.list())
}
