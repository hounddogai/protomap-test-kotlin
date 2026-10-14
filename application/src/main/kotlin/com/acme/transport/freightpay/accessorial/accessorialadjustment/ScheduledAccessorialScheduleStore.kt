package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled accessorial schedules by code. */
interface ScheduledAccessorialScheduleStore {
    fun find(code: String): ScheduledAccessorialSchedule?
    fun save(item: ScheduledAccessorialSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledAccessorialSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledAccessorialScheduleStore(
    var items: MutableMap<String, ScheduledAccessorialSchedule>,
    var order: MutableList<String>,
) : ScheduledAccessorialScheduleStore {
    override fun find(code: String): ScheduledAccessorialSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledAccessorialSchedule) {
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

    override fun list(): MutableList<ScheduledAccessorialSchedule> {
        val out = mutableListOf<ScheduledAccessorialSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledAccessorialScheduleStore(): ScheduledAccessorialScheduleStore {
    return MemoryScheduledAccessorialScheduleStore(
        mutableMapOf<String, ScheduledAccessorialSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledAccessorialScheduleStore(
    var inner: ScheduledAccessorialScheduleStore,
    var cache: TtlCache<ScheduledAccessorialSchedule>,
    var hits: Int,
    var misses: Int,
) : ScheduledAccessorialScheduleStore {
    override fun find(code: String): ScheduledAccessorialSchedule? {
        val hit = (cache.get(code) as ScheduledAccessorialSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledAccessorialSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledAccessorialSchedule> = inner.list()
}

fun newCachedScheduledAccessorialScheduleStore(inner: ScheduledAccessorialScheduleStore, ttl: Int, clock: Clock): ScheduledAccessorialScheduleStore {
    return CachedScheduledAccessorialScheduleStore(
        inner = inner,
        cache = newTtlCache<ScheduledAccessorialSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled accessorial schedules. Empty fields match all. */
data class ScheduledAccessorialScheduleQuery(
    var lane: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledAccessorialSchedule): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledAccessorialSchedule>): MutableList<ScheduledAccessorialSchedule> {
        val matched = filter(items) { candidate: ScheduledAccessorialSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledAccessorialSchedules(store: ScheduledAccessorialScheduleStore, query: ScheduledAccessorialScheduleQuery): MutableList<ScheduledAccessorialSchedule> {
    return query.apply(store.list())
}
