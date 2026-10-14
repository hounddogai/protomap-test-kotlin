package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal redelivery schedules by code. */
interface SeasonalRedeliveryScheduleStore {
    fun find(code: String): SeasonalRedeliverySchedule?
    fun save(item: SeasonalRedeliverySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRedeliverySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRedeliveryScheduleStore(
    var items: MutableMap<String, SeasonalRedeliverySchedule>,
    var order: MutableList<String>,
) : SeasonalRedeliveryScheduleStore {
    override fun find(code: String): SeasonalRedeliverySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRedeliverySchedule) {
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

    override fun list(): MutableList<SeasonalRedeliverySchedule> {
        val out = mutableListOf<SeasonalRedeliverySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRedeliveryScheduleStore(): SeasonalRedeliveryScheduleStore {
    return MemorySeasonalRedeliveryScheduleStore(
        mutableMapOf<String, SeasonalRedeliverySchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRedeliveryScheduleStore(
    var inner: SeasonalRedeliveryScheduleStore,
    var cache: TtlCache<SeasonalRedeliverySchedule>,
    var hits: Int,
    var misses: Int,
) : SeasonalRedeliveryScheduleStore {
    override fun find(code: String): SeasonalRedeliverySchedule? {
        val hit = (cache.get(code) as SeasonalRedeliverySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRedeliverySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRedeliverySchedule> = inner.list()
}

fun newCachedSeasonalRedeliveryScheduleStore(inner: SeasonalRedeliveryScheduleStore, ttl: Int, clock: Clock): SeasonalRedeliveryScheduleStore {
    return CachedSeasonalRedeliveryScheduleStore(
        inner = inner,
        cache = newTtlCache<SeasonalRedeliverySchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal redelivery schedules. Empty fields match all. */
data class SeasonalRedeliveryScheduleQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRedeliverySchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRedeliverySchedule>): MutableList<SeasonalRedeliverySchedule> {
        val matched = filter(items) { candidate: SeasonalRedeliverySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRedeliverySchedules(store: SeasonalRedeliveryScheduleStore, query: SeasonalRedeliveryScheduleQuery): MutableList<SeasonalRedeliverySchedule> {
    return query.apply(store.list())
}
