package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard schedules by code. */
interface TrailerYardScheduleStore {
    fun find(code: String): TrailerYardSchedule?
    fun save(item: TrailerYardSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardScheduleStore(
    var items: MutableMap<String, TrailerYardSchedule>,
    var order: MutableList<String>,
) : TrailerYardScheduleStore {
    override fun find(code: String): TrailerYardSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardSchedule) {
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

    override fun list(): MutableList<TrailerYardSchedule> {
        val out = mutableListOf<TrailerYardSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardScheduleStore(): TrailerYardScheduleStore {
    return MemoryTrailerYardScheduleStore(mutableMapOf<String, TrailerYardSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardScheduleStore(
    var inner: TrailerYardScheduleStore,
    var cache: TtlCache<TrailerYardSchedule>,
    var hits: Int,
    var misses: Int,
) : TrailerYardScheduleStore {
    override fun find(code: String): TrailerYardSchedule? {
        val hit = (cache.get(code) as TrailerYardSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardSchedule> = inner.list()
}

fun newCachedTrailerYardScheduleStore(inner: TrailerYardScheduleStore, ttl: Int, clock: Clock): TrailerYardScheduleStore {
    return CachedTrailerYardScheduleStore(
        inner = inner,
        cache = newTtlCache<TrailerYardSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard schedules. Empty fields match all. */
data class TrailerYardScheduleQuery(
    var category: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardSchedule): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardSchedule>): MutableList<TrailerYardSchedule> {
        val matched = filter(items) { candidate: TrailerYardSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardSchedules(store: TrailerYardScheduleStore, query: TrailerYardScheduleQuery): MutableList<TrailerYardSchedule> {
    return query.apply(store.list())
}
