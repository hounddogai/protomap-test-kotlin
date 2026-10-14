package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped capacity schedules by code. */
interface CappedCapacityScheduleStore {
    fun find(code: String): CappedCapacitySchedule?
    fun save(item: CappedCapacitySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCapacitySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCapacityScheduleStore(
    var items: MutableMap<String, CappedCapacitySchedule>,
    var order: MutableList<String>,
) : CappedCapacityScheduleStore {
    override fun find(code: String): CappedCapacitySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCapacitySchedule) {
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

    override fun list(): MutableList<CappedCapacitySchedule> {
        val out = mutableListOf<CappedCapacitySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCapacityScheduleStore(): CappedCapacityScheduleStore {
    return MemoryCappedCapacityScheduleStore(
        mutableMapOf<String, CappedCapacitySchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCapacityScheduleStore(
    var inner: CappedCapacityScheduleStore,
    var cache: TtlCache<CappedCapacitySchedule>,
    var hits: Int,
    var misses: Int,
) : CappedCapacityScheduleStore {
    override fun find(code: String): CappedCapacitySchedule? {
        val hit = (cache.get(code) as CappedCapacitySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCapacitySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCapacitySchedule> = inner.list()
}

fun newCachedCappedCapacityScheduleStore(inner: CappedCapacityScheduleStore, ttl: Int, clock: Clock): CappedCapacityScheduleStore {
    return CachedCappedCapacityScheduleStore(
        inner = inner,
        cache = newTtlCache<CappedCapacitySchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped capacity schedules. Empty fields match all. */
data class CappedCapacityScheduleQuery(
    var category: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCapacitySchedule): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedCapacitySchedule>): MutableList<CappedCapacitySchedule> {
        val matched = filter(items) { candidate: CappedCapacitySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCapacitySchedules(store: CappedCapacityScheduleStore, query: CappedCapacityScheduleQuery): MutableList<CappedCapacitySchedule> {
    return query.apply(store.list())
}
