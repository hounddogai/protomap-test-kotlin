package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global capacity schedules by code. */
interface GlobalCapacityScheduleStore {
    fun find(code: String): GlobalCapacitySchedule?
    fun save(item: GlobalCapacitySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCapacitySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCapacityScheduleStore(
    var items: MutableMap<String, GlobalCapacitySchedule>,
    var order: MutableList<String>,
) : GlobalCapacityScheduleStore {
    override fun find(code: String): GlobalCapacitySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCapacitySchedule) {
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

    override fun list(): MutableList<GlobalCapacitySchedule> {
        val out = mutableListOf<GlobalCapacitySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCapacityScheduleStore(): GlobalCapacityScheduleStore {
    return MemoryGlobalCapacityScheduleStore(
        mutableMapOf<String, GlobalCapacitySchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCapacityScheduleStore(
    var inner: GlobalCapacityScheduleStore,
    var cache: TtlCache<GlobalCapacitySchedule>,
    var hits: Int,
    var misses: Int,
) : GlobalCapacityScheduleStore {
    override fun find(code: String): GlobalCapacitySchedule? {
        val hit = (cache.get(code) as GlobalCapacitySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCapacitySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCapacitySchedule> = inner.list()
}

fun newCachedGlobalCapacityScheduleStore(inner: GlobalCapacityScheduleStore, ttl: Int, clock: Clock): GlobalCapacityScheduleStore {
    return CachedGlobalCapacityScheduleStore(
        inner = inner,
        cache = newTtlCache<GlobalCapacitySchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global capacity schedules. Empty fields match all. */
data class GlobalCapacityScheduleQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCapacitySchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCapacitySchedule>): MutableList<GlobalCapacitySchedule> {
        val matched = filter(items) { candidate: GlobalCapacitySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCapacitySchedules(store: GlobalCapacityScheduleStore, query: GlobalCapacityScheduleQuery): MutableList<GlobalCapacitySchedule> {
    return query.apply(store.list())
}
