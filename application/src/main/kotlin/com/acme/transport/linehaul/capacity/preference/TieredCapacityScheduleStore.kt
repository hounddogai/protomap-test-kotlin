package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered capacity schedules by code. */
interface TieredCapacityScheduleStore {
    fun find(code: String): TieredCapacitySchedule?
    fun save(item: TieredCapacitySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCapacitySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCapacityScheduleStore(
    var items: MutableMap<String, TieredCapacitySchedule>,
    var order: MutableList<String>,
) : TieredCapacityScheduleStore {
    override fun find(code: String): TieredCapacitySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCapacitySchedule) {
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

    override fun list(): MutableList<TieredCapacitySchedule> {
        val out = mutableListOf<TieredCapacitySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCapacityScheduleStore(): TieredCapacityScheduleStore {
    return MemoryTieredCapacityScheduleStore(
        mutableMapOf<String, TieredCapacitySchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCapacityScheduleStore(
    var inner: TieredCapacityScheduleStore,
    var cache: TtlCache<TieredCapacitySchedule>,
    var hits: Int,
    var misses: Int,
) : TieredCapacityScheduleStore {
    override fun find(code: String): TieredCapacitySchedule? {
        val hit = (cache.get(code) as TieredCapacitySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCapacitySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCapacitySchedule> = inner.list()
}

fun newCachedTieredCapacityScheduleStore(inner: TieredCapacityScheduleStore, ttl: Int, clock: Clock): TieredCapacityScheduleStore {
    return CachedTieredCapacityScheduleStore(
        inner = inner,
        cache = newTtlCache<TieredCapacitySchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered capacity schedules. Empty fields match all. */
data class TieredCapacityScheduleQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCapacitySchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<TieredCapacitySchedule>): MutableList<TieredCapacitySchedule> {
        val matched = filter(items) { candidate: TieredCapacitySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCapacitySchedules(store: TieredCapacityScheduleStore, query: TieredCapacityScheduleQuery): MutableList<TieredCapacitySchedule> {
    return query.apply(store.list())
}
