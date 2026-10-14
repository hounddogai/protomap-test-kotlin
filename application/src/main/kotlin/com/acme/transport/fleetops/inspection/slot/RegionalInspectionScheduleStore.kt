package com.acme.transport.fleetops.inspection.slot

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional inspection schedules by code. */
interface RegionalInspectionScheduleStore {
    fun find(code: String): RegionalInspectionSchedule?
    fun save(item: RegionalInspectionSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalInspectionSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalInspectionScheduleStore(
    var items: MutableMap<String, RegionalInspectionSchedule>,
    var order: MutableList<String>,
) : RegionalInspectionScheduleStore {
    override fun find(code: String): RegionalInspectionSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalInspectionSchedule) {
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

    override fun list(): MutableList<RegionalInspectionSchedule> {
        val out = mutableListOf<RegionalInspectionSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalInspectionScheduleStore(): RegionalInspectionScheduleStore {
    return MemoryRegionalInspectionScheduleStore(
        mutableMapOf<String, RegionalInspectionSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalInspectionScheduleStore(
    var inner: RegionalInspectionScheduleStore,
    var cache: TtlCache<RegionalInspectionSchedule>,
    var hits: Int,
    var misses: Int,
) : RegionalInspectionScheduleStore {
    override fun find(code: String): RegionalInspectionSchedule? {
        val hit = (cache.get(code) as RegionalInspectionSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalInspectionSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalInspectionSchedule> = inner.list()
}

fun newCachedRegionalInspectionScheduleStore(inner: RegionalInspectionScheduleStore, ttl: Int, clock: Clock): RegionalInspectionScheduleStore {
    return CachedRegionalInspectionScheduleStore(
        inner = inner,
        cache = newTtlCache<RegionalInspectionSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional inspection schedules. Empty fields match all. */
data class RegionalInspectionScheduleQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalInspectionSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalInspectionSchedule>): MutableList<RegionalInspectionSchedule> {
        val matched = filter(items) { candidate: RegionalInspectionSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalInspectionSchedules(store: RegionalInspectionScheduleStore, query: RegionalInspectionScheduleQuery): MutableList<RegionalInspectionSchedule> {
    return query.apply(store.list())
}
