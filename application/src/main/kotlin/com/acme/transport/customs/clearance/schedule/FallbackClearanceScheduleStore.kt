package com.acme.transport.customs.clearance.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback clearance schedules by code. */
interface FallbackClearanceScheduleStore {
    fun find(code: String): FallbackClearanceSchedule?
    fun save(item: FallbackClearanceSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackClearanceSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackClearanceScheduleStore(
    var items: MutableMap<String, FallbackClearanceSchedule>,
    var order: MutableList<String>,
) : FallbackClearanceScheduleStore {
    override fun find(code: String): FallbackClearanceSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackClearanceSchedule) {
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

    override fun list(): MutableList<FallbackClearanceSchedule> {
        val out = mutableListOf<FallbackClearanceSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackClearanceScheduleStore(): FallbackClearanceScheduleStore {
    return MemoryFallbackClearanceScheduleStore(
        mutableMapOf<String, FallbackClearanceSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackClearanceScheduleStore(
    var inner: FallbackClearanceScheduleStore,
    var cache: TtlCache<FallbackClearanceSchedule>,
    var hits: Int,
    var misses: Int,
) : FallbackClearanceScheduleStore {
    override fun find(code: String): FallbackClearanceSchedule? {
        val hit = (cache.get(code) as FallbackClearanceSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackClearanceSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackClearanceSchedule> = inner.list()
}

fun newCachedFallbackClearanceScheduleStore(inner: FallbackClearanceScheduleStore, ttl: Int, clock: Clock): FallbackClearanceScheduleStore {
    return CachedFallbackClearanceScheduleStore(
        inner = inner,
        cache = newTtlCache<FallbackClearanceSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback clearance schedules. Empty fields match all. */
data class FallbackClearanceScheduleQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackClearanceSchedule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackClearanceSchedule>): MutableList<FallbackClearanceSchedule> {
        val matched = filter(items) { candidate: FallbackClearanceSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackClearanceSchedules(store: FallbackClearanceScheduleStore, query: FallbackClearanceScheduleQuery): MutableList<FallbackClearanceSchedule> {
    return query.apply(store.list())
}
