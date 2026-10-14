package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient cross dock schedules by code. */
interface LenientCrossDockScheduleStore {
    fun find(code: String): LenientCrossDockSchedule?
    fun save(item: LenientCrossDockSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCrossDockSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCrossDockScheduleStore(
    var items: MutableMap<String, LenientCrossDockSchedule>,
    var order: MutableList<String>,
) : LenientCrossDockScheduleStore {
    override fun find(code: String): LenientCrossDockSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCrossDockSchedule) {
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

    override fun list(): MutableList<LenientCrossDockSchedule> {
        val out = mutableListOf<LenientCrossDockSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCrossDockScheduleStore(): LenientCrossDockScheduleStore {
    return MemoryLenientCrossDockScheduleStore(
        mutableMapOf<String, LenientCrossDockSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCrossDockScheduleStore(
    var inner: LenientCrossDockScheduleStore,
    var cache: TtlCache<LenientCrossDockSchedule>,
    var hits: Int,
    var misses: Int,
) : LenientCrossDockScheduleStore {
    override fun find(code: String): LenientCrossDockSchedule? {
        val hit = (cache.get(code) as LenientCrossDockSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCrossDockSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCrossDockSchedule> = inner.list()
}

fun newCachedLenientCrossDockScheduleStore(inner: LenientCrossDockScheduleStore, ttl: Int, clock: Clock): LenientCrossDockScheduleStore {
    return CachedLenientCrossDockScheduleStore(
        inner = inner,
        cache = newTtlCache<LenientCrossDockSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient cross dock schedules. Empty fields match all. */
data class LenientCrossDockScheduleQuery(
    var unit: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCrossDockSchedule): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCrossDockSchedule>): MutableList<LenientCrossDockSchedule> {
        val matched = filter(items) { candidate: LenientCrossDockSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCrossDockSchedules(store: LenientCrossDockScheduleStore, query: LenientCrossDockScheduleQuery): MutableList<LenientCrossDockSchedule> {
    return query.apply(store.list())
}
