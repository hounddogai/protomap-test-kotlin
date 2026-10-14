package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global zone schedules by code. */
interface GlobalZoneScheduleStore {
    fun find(code: String): GlobalZoneSchedule?
    fun save(item: GlobalZoneSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalZoneSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalZoneScheduleStore(
    var items: MutableMap<String, GlobalZoneSchedule>,
    var order: MutableList<String>,
) : GlobalZoneScheduleStore {
    override fun find(code: String): GlobalZoneSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalZoneSchedule) {
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

    override fun list(): MutableList<GlobalZoneSchedule> {
        val out = mutableListOf<GlobalZoneSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalZoneScheduleStore(): GlobalZoneScheduleStore {
    return MemoryGlobalZoneScheduleStore(mutableMapOf<String, GlobalZoneSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalZoneScheduleStore(
    var inner: GlobalZoneScheduleStore,
    var cache: TtlCache<GlobalZoneSchedule>,
    var hits: Int,
    var misses: Int,
) : GlobalZoneScheduleStore {
    override fun find(code: String): GlobalZoneSchedule? {
        val hit = (cache.get(code) as GlobalZoneSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalZoneSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalZoneSchedule> = inner.list()
}

fun newCachedGlobalZoneScheduleStore(inner: GlobalZoneScheduleStore, ttl: Int, clock: Clock): GlobalZoneScheduleStore {
    return CachedGlobalZoneScheduleStore(
        inner = inner,
        cache = newTtlCache<GlobalZoneSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global zone schedules. Empty fields match all. */
data class GlobalZoneScheduleQuery(
    var category: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalZoneSchedule): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalZoneSchedule>): MutableList<GlobalZoneSchedule> {
        val matched = filter(items) { candidate: GlobalZoneSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalZoneSchedules(store: GlobalZoneScheduleStore, query: GlobalZoneScheduleQuery): MutableList<GlobalZoneSchedule> {
    return query.apply(store.list())
}
