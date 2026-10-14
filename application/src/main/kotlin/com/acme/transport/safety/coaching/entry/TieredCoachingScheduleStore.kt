package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered coaching schedules by code. */
interface TieredCoachingScheduleStore {
    fun find(code: String): TieredCoachingSchedule?
    fun save(item: TieredCoachingSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCoachingSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCoachingScheduleStore(
    var items: MutableMap<String, TieredCoachingSchedule>,
    var order: MutableList<String>,
) : TieredCoachingScheduleStore {
    override fun find(code: String): TieredCoachingSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCoachingSchedule) {
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

    override fun list(): MutableList<TieredCoachingSchedule> {
        val out = mutableListOf<TieredCoachingSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCoachingScheduleStore(): TieredCoachingScheduleStore {
    return MemoryTieredCoachingScheduleStore(
        mutableMapOf<String, TieredCoachingSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCoachingScheduleStore(
    var inner: TieredCoachingScheduleStore,
    var cache: TtlCache<TieredCoachingSchedule>,
    var hits: Int,
    var misses: Int,
) : TieredCoachingScheduleStore {
    override fun find(code: String): TieredCoachingSchedule? {
        val hit = (cache.get(code) as TieredCoachingSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCoachingSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCoachingSchedule> = inner.list()
}

fun newCachedTieredCoachingScheduleStore(inner: TieredCoachingScheduleStore, ttl: Int, clock: Clock): TieredCoachingScheduleStore {
    return CachedTieredCoachingScheduleStore(
        inner = inner,
        cache = newTtlCache<TieredCoachingSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered coaching schedules. Empty fields match all. */
data class TieredCoachingScheduleQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCoachingSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCoachingSchedule>): MutableList<TieredCoachingSchedule> {
        val matched = filter(items) { candidate: TieredCoachingSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCoachingSchedules(store: TieredCoachingScheduleStore, query: TieredCoachingScheduleQuery): MutableList<TieredCoachingSchedule> {
    return query.apply(store.list())
}
