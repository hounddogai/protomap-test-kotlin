package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global coaching schedules by code. */
interface GlobalCoachingScheduleStore {
    fun find(code: String): GlobalCoachingSchedule?
    fun save(item: GlobalCoachingSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCoachingSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCoachingScheduleStore(
    var items: MutableMap<String, GlobalCoachingSchedule>,
    var order: MutableList<String>,
) : GlobalCoachingScheduleStore {
    override fun find(code: String): GlobalCoachingSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCoachingSchedule) {
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

    override fun list(): MutableList<GlobalCoachingSchedule> {
        val out = mutableListOf<GlobalCoachingSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCoachingScheduleStore(): GlobalCoachingScheduleStore {
    return MemoryGlobalCoachingScheduleStore(
        mutableMapOf<String, GlobalCoachingSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCoachingScheduleStore(
    var inner: GlobalCoachingScheduleStore,
    var cache: TtlCache<GlobalCoachingSchedule>,
    var hits: Int,
    var misses: Int,
) : GlobalCoachingScheduleStore {
    override fun find(code: String): GlobalCoachingSchedule? {
        val hit = (cache.get(code) as GlobalCoachingSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCoachingSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCoachingSchedule> = inner.list()
}

fun newCachedGlobalCoachingScheduleStore(inner: GlobalCoachingScheduleStore, ttl: Int, clock: Clock): GlobalCoachingScheduleStore {
    return CachedGlobalCoachingScheduleStore(
        inner = inner,
        cache = newTtlCache<GlobalCoachingSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global coaching schedules. Empty fields match all. */
data class GlobalCoachingScheduleQuery(
    var segment: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCoachingSchedule): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCoachingSchedule>): MutableList<GlobalCoachingSchedule> {
        val matched = filter(items) { candidate: GlobalCoachingSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCoachingSchedules(store: GlobalCoachingScheduleStore, query: GlobalCoachingScheduleQuery): MutableList<GlobalCoachingSchedule> {
    return query.apply(store.list())
}
