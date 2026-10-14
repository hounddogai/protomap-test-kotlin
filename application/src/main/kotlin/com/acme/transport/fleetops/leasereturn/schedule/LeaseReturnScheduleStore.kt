package com.acme.transport.fleetops.leasereturn.schedule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return schedules by code. */
interface LeaseReturnScheduleStore {
    fun find(code: String): LeaseReturnSchedule?
    fun save(item: LeaseReturnSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnScheduleStore(
    var items: MutableMap<String, LeaseReturnSchedule>,
    var order: MutableList<String>,
) : LeaseReturnScheduleStore {
    override fun find(code: String): LeaseReturnSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnSchedule) {
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

    override fun list(): MutableList<LeaseReturnSchedule> {
        val out = mutableListOf<LeaseReturnSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnScheduleStore(): LeaseReturnScheduleStore {
    return MemoryLeaseReturnScheduleStore(mutableMapOf<String, LeaseReturnSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnScheduleStore(
    var inner: LeaseReturnScheduleStore,
    var cache: TtlCache<LeaseReturnSchedule>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnScheduleStore {
    override fun find(code: String): LeaseReturnSchedule? {
        val hit = (cache.get(code) as LeaseReturnSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnSchedule> = inner.list()
}

fun newCachedLeaseReturnScheduleStore(inner: LeaseReturnScheduleStore, ttl: Int, clock: Clock): LeaseReturnScheduleStore {
    return CachedLeaseReturnScheduleStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return schedules. Empty fields match all. */
data class LeaseReturnScheduleQuery(
    var locale: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnSchedule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnSchedule>): MutableList<LeaseReturnSchedule> {
        val matched = filter(items) { candidate: LeaseReturnSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnSchedules(store: LeaseReturnScheduleStore, query: LeaseReturnScheduleQuery): MutableList<LeaseReturnSchedule> {
    return query.apply(store.list())
}
