package com.acme.transport.crewing.swap.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap schedules by code. */
interface SwapScheduleStore {
    fun find(code: String): SwapSchedule?
    fun save(item: SwapSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapScheduleStore(
    var items: MutableMap<String, SwapSchedule>,
    var order: MutableList<String>,
) : SwapScheduleStore {
    override fun find(code: String): SwapSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapSchedule) {
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

    override fun list(): MutableList<SwapSchedule> {
        val out = mutableListOf<SwapSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapScheduleStore(): SwapScheduleStore {
    return MemorySwapScheduleStore(mutableMapOf<String, SwapSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapScheduleStore(
    var inner: SwapScheduleStore,
    var cache: TtlCache<SwapSchedule>,
    var hits: Int,
    var misses: Int,
) : SwapScheduleStore {
    override fun find(code: String): SwapSchedule? {
        val hit = (cache.get(code) as SwapSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapSchedule> = inner.list()
}

fun newCachedSwapScheduleStore(inner: SwapScheduleStore, ttl: Int, clock: Clock): SwapScheduleStore {
    return CachedSwapScheduleStore(
        inner = inner,
        cache = newTtlCache<SwapSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap schedules. Empty fields match all. */
data class SwapScheduleQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapSchedule>): MutableList<SwapSchedule> {
        val matched = filter(items) { candidate: SwapSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapSchedules(store: SwapScheduleStore, query: SwapScheduleQuery): MutableList<SwapSchedule> {
    return query.apply(store.list())
}
