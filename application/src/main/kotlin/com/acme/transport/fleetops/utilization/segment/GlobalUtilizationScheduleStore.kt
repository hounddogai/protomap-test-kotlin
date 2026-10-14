package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global utilization schedules by code. */
interface GlobalUtilizationScheduleStore {
    fun find(code: String): GlobalUtilizationSchedule?
    fun save(item: GlobalUtilizationSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalUtilizationSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalUtilizationScheduleStore(
    var items: MutableMap<String, GlobalUtilizationSchedule>,
    var order: MutableList<String>,
) : GlobalUtilizationScheduleStore {
    override fun find(code: String): GlobalUtilizationSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalUtilizationSchedule) {
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

    override fun list(): MutableList<GlobalUtilizationSchedule> {
        val out = mutableListOf<GlobalUtilizationSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalUtilizationScheduleStore(): GlobalUtilizationScheduleStore {
    return MemoryGlobalUtilizationScheduleStore(
        mutableMapOf<String, GlobalUtilizationSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalUtilizationScheduleStore(
    var inner: GlobalUtilizationScheduleStore,
    var cache: TtlCache<GlobalUtilizationSchedule>,
    var hits: Int,
    var misses: Int,
) : GlobalUtilizationScheduleStore {
    override fun find(code: String): GlobalUtilizationSchedule? {
        val hit = (cache.get(code) as GlobalUtilizationSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalUtilizationSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalUtilizationSchedule> = inner.list()
}

fun newCachedGlobalUtilizationScheduleStore(inner: GlobalUtilizationScheduleStore, ttl: Int, clock: Clock): GlobalUtilizationScheduleStore {
    return CachedGlobalUtilizationScheduleStore(
        inner = inner,
        cache = newTtlCache<GlobalUtilizationSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global utilization schedules. Empty fields match all. */
data class GlobalUtilizationScheduleQuery(
    var source: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalUtilizationSchedule): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<GlobalUtilizationSchedule>): MutableList<GlobalUtilizationSchedule> {
        val matched = filter(items) { candidate: GlobalUtilizationSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalUtilizationSchedules(store: GlobalUtilizationScheduleStore, query: GlobalUtilizationScheduleQuery): MutableList<GlobalUtilizationSchedule> {
    return query.apply(store.list())
}
