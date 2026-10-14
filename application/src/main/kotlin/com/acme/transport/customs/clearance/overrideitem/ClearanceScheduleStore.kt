package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance schedules by code. */
interface ClearanceScheduleStore {
    fun find(code: String): ClearanceSchedule?
    fun save(item: ClearanceSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceScheduleStore(
    var items: MutableMap<String, ClearanceSchedule>,
    var order: MutableList<String>,
) : ClearanceScheduleStore {
    override fun find(code: String): ClearanceSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceSchedule) {
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

    override fun list(): MutableList<ClearanceSchedule> {
        val out = mutableListOf<ClearanceSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceScheduleStore(): ClearanceScheduleStore {
    return MemoryClearanceScheduleStore(mutableMapOf<String, ClearanceSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceScheduleStore(
    var inner: ClearanceScheduleStore,
    var cache: TtlCache<ClearanceSchedule>,
    var hits: Int,
    var misses: Int,
) : ClearanceScheduleStore {
    override fun find(code: String): ClearanceSchedule? {
        val hit = (cache.get(code) as ClearanceSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceSchedule> = inner.list()
}

fun newCachedClearanceScheduleStore(inner: ClearanceScheduleStore, ttl: Int, clock: Clock): ClearanceScheduleStore {
    return CachedClearanceScheduleStore(
        inner = inner,
        cache = newTtlCache<ClearanceSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance schedules. Empty fields match all. */
data class ClearanceScheduleQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceSchedule>): MutableList<ClearanceSchedule> {
        val matched = filter(items) { candidate: ClearanceSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceSchedules(store: ClearanceScheduleStore, query: ClearanceScheduleQuery): MutableList<ClearanceSchedule> {
    return query.apply(store.list())
}
