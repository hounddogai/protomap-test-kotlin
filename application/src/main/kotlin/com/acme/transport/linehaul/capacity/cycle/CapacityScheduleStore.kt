package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity schedules by code. */
interface CapacityScheduleStore {
    fun find(code: String): CapacitySchedule?
    fun save(item: CapacitySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacitySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityScheduleStore(
    var items: MutableMap<String, CapacitySchedule>,
    var order: MutableList<String>,
) : CapacityScheduleStore {
    override fun find(code: String): CapacitySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacitySchedule) {
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

    override fun list(): MutableList<CapacitySchedule> {
        val out = mutableListOf<CapacitySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityScheduleStore(): CapacityScheduleStore {
    return MemoryCapacityScheduleStore(mutableMapOf<String, CapacitySchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityScheduleStore(
    var inner: CapacityScheduleStore,
    var cache: TtlCache<CapacitySchedule>,
    var hits: Int,
    var misses: Int,
) : CapacityScheduleStore {
    override fun find(code: String): CapacitySchedule? {
        val hit = (cache.get(code) as CapacitySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacitySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacitySchedule> = inner.list()
}

fun newCachedCapacityScheduleStore(inner: CapacityScheduleStore, ttl: Int, clock: Clock): CapacityScheduleStore {
    return CachedCapacityScheduleStore(
        inner = inner,
        cache = newTtlCache<CapacitySchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity schedules. Empty fields match all. */
data class CapacityScheduleQuery(
    var unit: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacitySchedule): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<CapacitySchedule>): MutableList<CapacitySchedule> {
        val matched = filter(items) { candidate: CapacitySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacitySchedules(store: CapacityScheduleStore, query: CapacityScheduleQuery): MutableList<CapacitySchedule> {
    return query.apply(store.list())
}
