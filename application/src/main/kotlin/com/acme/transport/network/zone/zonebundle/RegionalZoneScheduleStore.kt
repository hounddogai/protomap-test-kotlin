package com.acme.transport.network.zone.zonebundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional zone schedules by code. */
interface RegionalZoneScheduleStore {
    fun find(code: String): RegionalZoneSchedule?
    fun save(item: RegionalZoneSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalZoneSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalZoneScheduleStore(
    var items: MutableMap<String, RegionalZoneSchedule>,
    var order: MutableList<String>,
) : RegionalZoneScheduleStore {
    override fun find(code: String): RegionalZoneSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalZoneSchedule) {
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

    override fun list(): MutableList<RegionalZoneSchedule> {
        val out = mutableListOf<RegionalZoneSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalZoneScheduleStore(): RegionalZoneScheduleStore {
    return MemoryRegionalZoneScheduleStore(
        mutableMapOf<String, RegionalZoneSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalZoneScheduleStore(
    var inner: RegionalZoneScheduleStore,
    var cache: TtlCache<RegionalZoneSchedule>,
    var hits: Int,
    var misses: Int,
) : RegionalZoneScheduleStore {
    override fun find(code: String): RegionalZoneSchedule? {
        val hit = (cache.get(code) as RegionalZoneSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalZoneSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalZoneSchedule> = inner.list()
}

fun newCachedRegionalZoneScheduleStore(inner: RegionalZoneScheduleStore, ttl: Int, clock: Clock): RegionalZoneScheduleStore {
    return CachedRegionalZoneScheduleStore(
        inner = inner,
        cache = newTtlCache<RegionalZoneSchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional zone schedules. Empty fields match all. */
data class RegionalZoneScheduleQuery(
    var locale: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalZoneSchedule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalZoneSchedule>): MutableList<RegionalZoneSchedule> {
        val matched = filter(items) { candidate: RegionalZoneSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalZoneSchedules(store: RegionalZoneScheduleStore, query: RegionalZoneScheduleQuery): MutableList<RegionalZoneSchedule> {
    return query.apply(store.list())
}
