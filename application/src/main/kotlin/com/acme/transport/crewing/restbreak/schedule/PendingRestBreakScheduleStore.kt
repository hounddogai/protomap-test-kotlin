package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending rest break schedules by code. */
interface PendingRestBreakScheduleStore {
    fun find(code: String): PendingRestBreakSchedule?
    fun save(item: PendingRestBreakSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRestBreakSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRestBreakScheduleStore(
    var items: MutableMap<String, PendingRestBreakSchedule>,
    var order: MutableList<String>,
) : PendingRestBreakScheduleStore {
    override fun find(code: String): PendingRestBreakSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRestBreakSchedule) {
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

    override fun list(): MutableList<PendingRestBreakSchedule> {
        val out = mutableListOf<PendingRestBreakSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRestBreakScheduleStore(): PendingRestBreakScheduleStore {
    return MemoryPendingRestBreakScheduleStore(
        mutableMapOf<String, PendingRestBreakSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRestBreakScheduleStore(
    var inner: PendingRestBreakScheduleStore,
    var cache: TtlCache<PendingRestBreakSchedule>,
    var hits: Int,
    var misses: Int,
) : PendingRestBreakScheduleStore {
    override fun find(code: String): PendingRestBreakSchedule? {
        val hit = (cache.get(code) as PendingRestBreakSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRestBreakSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRestBreakSchedule> = inner.list()
}

fun newCachedPendingRestBreakScheduleStore(inner: PendingRestBreakScheduleStore, ttl: Int, clock: Clock): PendingRestBreakScheduleStore {
    return CachedPendingRestBreakScheduleStore(
        inner = inner,
        cache = newTtlCache<PendingRestBreakSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending rest break schedules. Empty fields match all. */
data class PendingRestBreakScheduleQuery(
    var channel: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRestBreakSchedule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingRestBreakSchedule>): MutableList<PendingRestBreakSchedule> {
        val matched = filter(items) { candidate: PendingRestBreakSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRestBreakSchedules(store: PendingRestBreakScheduleStore, query: PendingRestBreakScheduleQuery): MutableList<PendingRestBreakSchedule> {
    return query.apply(store.list())
}
