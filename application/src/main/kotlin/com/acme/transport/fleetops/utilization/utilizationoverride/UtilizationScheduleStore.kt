package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization schedules by code. */
interface UtilizationScheduleStore {
    fun find(code: String): UtilizationSchedule?
    fun save(item: UtilizationSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationScheduleStore(
    var items: MutableMap<String, UtilizationSchedule>,
    var order: MutableList<String>,
) : UtilizationScheduleStore {
    override fun find(code: String): UtilizationSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationSchedule) {
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

    override fun list(): MutableList<UtilizationSchedule> {
        val out = mutableListOf<UtilizationSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationScheduleStore(): UtilizationScheduleStore {
    return MemoryUtilizationScheduleStore(mutableMapOf<String, UtilizationSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationScheduleStore(
    var inner: UtilizationScheduleStore,
    var cache: TtlCache<UtilizationSchedule>,
    var hits: Int,
    var misses: Int,
) : UtilizationScheduleStore {
    override fun find(code: String): UtilizationSchedule? {
        val hit = (cache.get(code) as UtilizationSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationSchedule> = inner.list()
}

fun newCachedUtilizationScheduleStore(inner: UtilizationScheduleStore, ttl: Int, clock: Clock): UtilizationScheduleStore {
    return CachedUtilizationScheduleStore(
        inner = inner,
        cache = newTtlCache<UtilizationSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization schedules. Empty fields match all. */
data class UtilizationScheduleQuery(
    var lane: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationSchedule): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationSchedule>): MutableList<UtilizationSchedule> {
        val matched = filter(items) { candidate: UtilizationSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationSchedules(store: UtilizationScheduleStore, query: UtilizationScheduleQuery): MutableList<UtilizationSchedule> {
    return query.apply(store.list())
}
