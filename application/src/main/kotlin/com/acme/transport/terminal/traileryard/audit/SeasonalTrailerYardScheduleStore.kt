package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal trailer yard schedules by code. */
interface SeasonalTrailerYardScheduleStore {
    fun find(code: String): SeasonalTrailerYardSchedule?
    fun save(item: SeasonalTrailerYardSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTrailerYardSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTrailerYardScheduleStore(
    var items: MutableMap<String, SeasonalTrailerYardSchedule>,
    var order: MutableList<String>,
) : SeasonalTrailerYardScheduleStore {
    override fun find(code: String): SeasonalTrailerYardSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTrailerYardSchedule) {
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

    override fun list(): MutableList<SeasonalTrailerYardSchedule> {
        val out = mutableListOf<SeasonalTrailerYardSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTrailerYardScheduleStore(): SeasonalTrailerYardScheduleStore {
    return MemorySeasonalTrailerYardScheduleStore(
        mutableMapOf<String, SeasonalTrailerYardSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTrailerYardScheduleStore(
    var inner: SeasonalTrailerYardScheduleStore,
    var cache: TtlCache<SeasonalTrailerYardSchedule>,
    var hits: Int,
    var misses: Int,
) : SeasonalTrailerYardScheduleStore {
    override fun find(code: String): SeasonalTrailerYardSchedule? {
        val hit = (cache.get(code) as SeasonalTrailerYardSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTrailerYardSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTrailerYardSchedule> = inner.list()
}

fun newCachedSeasonalTrailerYardScheduleStore(inner: SeasonalTrailerYardScheduleStore, ttl: Int, clock: Clock): SeasonalTrailerYardScheduleStore {
    return CachedSeasonalTrailerYardScheduleStore(
        inner = inner,
        cache = newTtlCache<SeasonalTrailerYardSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal trailer yard schedules. Empty fields match all. */
data class SeasonalTrailerYardScheduleQuery(
    var category: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTrailerYardSchedule): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTrailerYardSchedule>): MutableList<SeasonalTrailerYardSchedule> {
        val matched = filter(items) { candidate: SeasonalTrailerYardSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTrailerYardSchedules(store: SeasonalTrailerYardScheduleStore, query: SeasonalTrailerYardScheduleQuery): MutableList<SeasonalTrailerYardSchedule> {
    return query.apply(store.list())
}
