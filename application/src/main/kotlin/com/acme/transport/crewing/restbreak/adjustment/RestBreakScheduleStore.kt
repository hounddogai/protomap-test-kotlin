package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break schedules by code. */
interface RestBreakScheduleStore {
    fun find(code: String): RestBreakSchedule?
    fun save(item: RestBreakSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakScheduleStore(
    var items: MutableMap<String, RestBreakSchedule>,
    var order: MutableList<String>,
) : RestBreakScheduleStore {
    override fun find(code: String): RestBreakSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakSchedule) {
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

    override fun list(): MutableList<RestBreakSchedule> {
        val out = mutableListOf<RestBreakSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakScheduleStore(): RestBreakScheduleStore {
    return MemoryRestBreakScheduleStore(mutableMapOf<String, RestBreakSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakScheduleStore(
    var inner: RestBreakScheduleStore,
    var cache: TtlCache<RestBreakSchedule>,
    var hits: Int,
    var misses: Int,
) : RestBreakScheduleStore {
    override fun find(code: String): RestBreakSchedule? {
        val hit = (cache.get(code) as RestBreakSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakSchedule> = inner.list()
}

fun newCachedRestBreakScheduleStore(inner: RestBreakScheduleStore, ttl: Int, clock: Clock): RestBreakScheduleStore {
    return CachedRestBreakScheduleStore(
        inner = inner,
        cache = newTtlCache<RestBreakSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break schedules. Empty fields match all. */
data class RestBreakScheduleQuery(
    var channel: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakSchedule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<RestBreakSchedule>): MutableList<RestBreakSchedule> {
        val matched = filter(items) { candidate: RestBreakSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakSchedules(store: RestBreakScheduleStore, query: RestBreakScheduleQuery): MutableList<RestBreakSchedule> {
    return query.apply(store.list())
}
