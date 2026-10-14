package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional parcel schedules by code. */
interface RegionalParcelScheduleStore {
    fun find(code: String): RegionalParcelSchedule?
    fun save(item: RegionalParcelSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalParcelSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalParcelScheduleStore(
    var items: MutableMap<String, RegionalParcelSchedule>,
    var order: MutableList<String>,
) : RegionalParcelScheduleStore {
    override fun find(code: String): RegionalParcelSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalParcelSchedule) {
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

    override fun list(): MutableList<RegionalParcelSchedule> {
        val out = mutableListOf<RegionalParcelSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalParcelScheduleStore(): RegionalParcelScheduleStore {
    return MemoryRegionalParcelScheduleStore(
        mutableMapOf<String, RegionalParcelSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalParcelScheduleStore(
    var inner: RegionalParcelScheduleStore,
    var cache: TtlCache<RegionalParcelSchedule>,
    var hits: Int,
    var misses: Int,
) : RegionalParcelScheduleStore {
    override fun find(code: String): RegionalParcelSchedule? {
        val hit = (cache.get(code) as RegionalParcelSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalParcelSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalParcelSchedule> = inner.list()
}

fun newCachedRegionalParcelScheduleStore(inner: RegionalParcelScheduleStore, ttl: Int, clock: Clock): RegionalParcelScheduleStore {
    return CachedRegionalParcelScheduleStore(
        inner = inner,
        cache = newTtlCache<RegionalParcelSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional parcel schedules. Empty fields match all. */
data class RegionalParcelScheduleQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalParcelSchedule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalParcelSchedule>): MutableList<RegionalParcelSchedule> {
        val matched = filter(items) { candidate: RegionalParcelSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalParcelSchedules(store: RegionalParcelScheduleStore, query: RegionalParcelScheduleQuery): MutableList<RegionalParcelSchedule> {
    return query.apply(store.list())
}
