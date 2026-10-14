package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive swap schedules by code. */
interface AdaptiveSwapScheduleStore {
    fun find(code: String): AdaptiveSwapSchedule?
    fun save(item: AdaptiveSwapSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveSwapSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveSwapScheduleStore(
    var items: MutableMap<String, AdaptiveSwapSchedule>,
    var order: MutableList<String>,
) : AdaptiveSwapScheduleStore {
    override fun find(code: String): AdaptiveSwapSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveSwapSchedule) {
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

    override fun list(): MutableList<AdaptiveSwapSchedule> {
        val out = mutableListOf<AdaptiveSwapSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveSwapScheduleStore(): AdaptiveSwapScheduleStore {
    return MemoryAdaptiveSwapScheduleStore(
        mutableMapOf<String, AdaptiveSwapSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveSwapScheduleStore(
    var inner: AdaptiveSwapScheduleStore,
    var cache: TtlCache<AdaptiveSwapSchedule>,
    var hits: Int,
    var misses: Int,
) : AdaptiveSwapScheduleStore {
    override fun find(code: String): AdaptiveSwapSchedule? {
        val hit = (cache.get(code) as AdaptiveSwapSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveSwapSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveSwapSchedule> = inner.list()
}

fun newCachedAdaptiveSwapScheduleStore(inner: AdaptiveSwapScheduleStore, ttl: Int, clock: Clock): AdaptiveSwapScheduleStore {
    return CachedAdaptiveSwapScheduleStore(
        inner = inner,
        cache = newTtlCache<AdaptiveSwapSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive swap schedules. Empty fields match all. */
data class AdaptiveSwapScheduleQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveSwapSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveSwapSchedule>): MutableList<AdaptiveSwapSchedule> {
        val matched = filter(items) { candidate: AdaptiveSwapSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveSwapSchedules(store: AdaptiveSwapScheduleStore, query: AdaptiveSwapScheduleQuery): MutableList<AdaptiveSwapSchedule> {
    return query.apply(store.list())
}
