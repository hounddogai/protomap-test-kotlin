package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability schedules by code. */
interface AvailabilityScheduleStore {
    fun find(code: String): AvailabilitySchedule?
    fun save(item: AvailabilitySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilitySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityScheduleStore(
    var items: MutableMap<String, AvailabilitySchedule>,
    var order: MutableList<String>,
) : AvailabilityScheduleStore {
    override fun find(code: String): AvailabilitySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilitySchedule) {
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

    override fun list(): MutableList<AvailabilitySchedule> {
        val out = mutableListOf<AvailabilitySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityScheduleStore(): AvailabilityScheduleStore {
    return MemoryAvailabilityScheduleStore(
        mutableMapOf<String, AvailabilitySchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityScheduleStore(
    var inner: AvailabilityScheduleStore,
    var cache: TtlCache<AvailabilitySchedule>,
    var hits: Int,
    var misses: Int,
) : AvailabilityScheduleStore {
    override fun find(code: String): AvailabilitySchedule? {
        val hit = (cache.get(code) as AvailabilitySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilitySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilitySchedule> = inner.list()
}

fun newCachedAvailabilityScheduleStore(inner: AvailabilityScheduleStore, ttl: Int, clock: Clock): AvailabilityScheduleStore {
    return CachedAvailabilityScheduleStore(
        inner = inner,
        cache = newTtlCache<AvailabilitySchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability schedules. Empty fields match all. */
data class AvailabilityScheduleQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilitySchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilitySchedule>): MutableList<AvailabilitySchedule> {
        val matched = filter(items) { candidate: AvailabilitySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilitySchedules(store: AvailabilityScheduleStore, query: AvailabilityScheduleQuery): MutableList<AvailabilitySchedule> {
    return query.apply(store.list())
}
