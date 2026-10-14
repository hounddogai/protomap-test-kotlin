package com.acme.transport.crewing.dutyperiod.notice

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period schedules by code. */
interface DutyPeriodScheduleStore {
    fun find(code: String): DutyPeriodSchedule?
    fun save(item: DutyPeriodSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodScheduleStore(
    var items: MutableMap<String, DutyPeriodSchedule>,
    var order: MutableList<String>,
) : DutyPeriodScheduleStore {
    override fun find(code: String): DutyPeriodSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodSchedule) {
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

    override fun list(): MutableList<DutyPeriodSchedule> {
        val out = mutableListOf<DutyPeriodSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodScheduleStore(): DutyPeriodScheduleStore {
    return MemoryDutyPeriodScheduleStore(mutableMapOf<String, DutyPeriodSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodScheduleStore(
    var inner: DutyPeriodScheduleStore,
    var cache: TtlCache<DutyPeriodSchedule>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodScheduleStore {
    override fun find(code: String): DutyPeriodSchedule? {
        val hit = (cache.get(code) as DutyPeriodSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodSchedule> = inner.list()
}

fun newCachedDutyPeriodScheduleStore(inner: DutyPeriodScheduleStore, ttl: Int, clock: Clock): DutyPeriodScheduleStore {
    return CachedDutyPeriodScheduleStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period schedules. Empty fields match all. */
data class DutyPeriodScheduleQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodSchedule>): MutableList<DutyPeriodSchedule> {
        val matched = filter(items) { candidate: DutyPeriodSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodSchedules(store: DutyPeriodScheduleStore, query: DutyPeriodScheduleQuery): MutableList<DutyPeriodSchedule> {
    return query.apply(store.list())
}
