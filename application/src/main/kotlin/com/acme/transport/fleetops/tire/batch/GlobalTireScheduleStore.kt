package com.acme.transport.fleetops.tire.batch

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global tire schedules by code. */
interface GlobalTireScheduleStore {
    fun find(code: String): GlobalTireSchedule?
    fun save(item: GlobalTireSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTireSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTireScheduleStore(
    var items: MutableMap<String, GlobalTireSchedule>,
    var order: MutableList<String>,
) : GlobalTireScheduleStore {
    override fun find(code: String): GlobalTireSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTireSchedule) {
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

    override fun list(): MutableList<GlobalTireSchedule> {
        val out = mutableListOf<GlobalTireSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTireScheduleStore(): GlobalTireScheduleStore {
    return MemoryGlobalTireScheduleStore(mutableMapOf<String, GlobalTireSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTireScheduleStore(
    var inner: GlobalTireScheduleStore,
    var cache: TtlCache<GlobalTireSchedule>,
    var hits: Int,
    var misses: Int,
) : GlobalTireScheduleStore {
    override fun find(code: String): GlobalTireSchedule? {
        val hit = (cache.get(code) as GlobalTireSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTireSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTireSchedule> = inner.list()
}

fun newCachedGlobalTireScheduleStore(inner: GlobalTireScheduleStore, ttl: Int, clock: Clock): GlobalTireScheduleStore {
    return CachedGlobalTireScheduleStore(
        inner = inner,
        cache = newTtlCache<GlobalTireSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global tire schedules. Empty fields match all. */
data class GlobalTireScheduleQuery(
    var unit: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTireSchedule): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalTireSchedule>): MutableList<GlobalTireSchedule> {
        val matched = filter(items) { candidate: GlobalTireSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTireSchedules(store: GlobalTireScheduleStore, query: GlobalTireScheduleQuery): MutableList<GlobalTireSchedule> {
    return query.apply(store.list())
}
