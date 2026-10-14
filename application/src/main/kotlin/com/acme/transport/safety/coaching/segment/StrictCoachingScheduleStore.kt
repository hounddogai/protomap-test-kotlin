package com.acme.transport.safety.coaching.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict coaching schedules by code. */
interface StrictCoachingScheduleStore {
    fun find(code: String): StrictCoachingSchedule?
    fun save(item: StrictCoachingSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCoachingSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCoachingScheduleStore(
    var items: MutableMap<String, StrictCoachingSchedule>,
    var order: MutableList<String>,
) : StrictCoachingScheduleStore {
    override fun find(code: String): StrictCoachingSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCoachingSchedule) {
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

    override fun list(): MutableList<StrictCoachingSchedule> {
        val out = mutableListOf<StrictCoachingSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCoachingScheduleStore(): StrictCoachingScheduleStore {
    return MemoryStrictCoachingScheduleStore(
        mutableMapOf<String, StrictCoachingSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCoachingScheduleStore(
    var inner: StrictCoachingScheduleStore,
    var cache: TtlCache<StrictCoachingSchedule>,
    var hits: Int,
    var misses: Int,
) : StrictCoachingScheduleStore {
    override fun find(code: String): StrictCoachingSchedule? {
        val hit = (cache.get(code) as StrictCoachingSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCoachingSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCoachingSchedule> = inner.list()
}

fun newCachedStrictCoachingScheduleStore(inner: StrictCoachingScheduleStore, ttl: Int, clock: Clock): StrictCoachingScheduleStore {
    return CachedStrictCoachingScheduleStore(
        inner = inner,
        cache = newTtlCache<StrictCoachingSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict coaching schedules. Empty fields match all. */
data class StrictCoachingScheduleQuery(
    var channel: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCoachingSchedule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictCoachingSchedule>): MutableList<StrictCoachingSchedule> {
        val matched = filter(items) { candidate: StrictCoachingSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCoachingSchedules(store: StrictCoachingScheduleStore, query: StrictCoachingScheduleQuery): MutableList<StrictCoachingSchedule> {
    return query.apply(store.list())
}
