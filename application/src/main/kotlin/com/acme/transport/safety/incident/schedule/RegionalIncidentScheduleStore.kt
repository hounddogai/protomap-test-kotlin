package com.acme.transport.safety.incident.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional incident schedules by code. */
interface RegionalIncidentScheduleStore {
    fun find(code: String): RegionalIncidentSchedule?
    fun save(item: RegionalIncidentSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalIncidentSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalIncidentScheduleStore(
    var items: MutableMap<String, RegionalIncidentSchedule>,
    var order: MutableList<String>,
) : RegionalIncidentScheduleStore {
    override fun find(code: String): RegionalIncidentSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalIncidentSchedule) {
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

    override fun list(): MutableList<RegionalIncidentSchedule> {
        val out = mutableListOf<RegionalIncidentSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalIncidentScheduleStore(): RegionalIncidentScheduleStore {
    return MemoryRegionalIncidentScheduleStore(
        mutableMapOf<String, RegionalIncidentSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalIncidentScheduleStore(
    var inner: RegionalIncidentScheduleStore,
    var cache: TtlCache<RegionalIncidentSchedule>,
    var hits: Int,
    var misses: Int,
) : RegionalIncidentScheduleStore {
    override fun find(code: String): RegionalIncidentSchedule? {
        val hit = (cache.get(code) as RegionalIncidentSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalIncidentSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalIncidentSchedule> = inner.list()
}

fun newCachedRegionalIncidentScheduleStore(inner: RegionalIncidentScheduleStore, ttl: Int, clock: Clock): RegionalIncidentScheduleStore {
    return CachedRegionalIncidentScheduleStore(
        inner = inner,
        cache = newTtlCache<RegionalIncidentSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional incident schedules. Empty fields match all. */
data class RegionalIncidentScheduleQuery(
    var channel: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalIncidentSchedule): Boolean {
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

    fun apply(items: MutableList<RegionalIncidentSchedule>): MutableList<RegionalIncidentSchedule> {
        val matched = filter(items) { candidate: RegionalIncidentSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalIncidentSchedules(store: RegionalIncidentScheduleStore, query: RegionalIncidentScheduleQuery): MutableList<RegionalIncidentSchedule> {
    return query.apply(store.list())
}
