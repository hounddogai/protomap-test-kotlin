package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone schedules by code. */
interface ZoneScheduleStore {
    fun find(code: String): ZoneSchedule?
    fun save(item: ZoneSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneScheduleStore(
    var items: MutableMap<String, ZoneSchedule>,
    var order: MutableList<String>,
) : ZoneScheduleStore {
    override fun find(code: String): ZoneSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneSchedule) {
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

    override fun list(): MutableList<ZoneSchedule> {
        val out = mutableListOf<ZoneSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneScheduleStore(): ZoneScheduleStore {
    return MemoryZoneScheduleStore(mutableMapOf<String, ZoneSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneScheduleStore(
    var inner: ZoneScheduleStore,
    var cache: TtlCache<ZoneSchedule>,
    var hits: Int,
    var misses: Int,
) : ZoneScheduleStore {
    override fun find(code: String): ZoneSchedule? {
        val hit = (cache.get(code) as ZoneSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneSchedule> = inner.list()
}

fun newCachedZoneScheduleStore(inner: ZoneScheduleStore, ttl: Int, clock: Clock): ZoneScheduleStore {
    return CachedZoneScheduleStore(
        inner = inner,
        cache = newTtlCache<ZoneSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone schedules. Empty fields match all. */
data class ZoneScheduleQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneSchedule>): MutableList<ZoneSchedule> {
        val matched = filter(items) { candidate: ZoneSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneSchedules(store: ZoneScheduleStore, query: ZoneScheduleQuery): MutableList<ZoneSchedule> {
    return query.apply(store.list())
}
