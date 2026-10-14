package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire schedules by code. */
interface TireScheduleStore {
    fun find(code: String): TireSchedule?
    fun save(item: TireSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireScheduleStore(
    var items: MutableMap<String, TireSchedule>,
    var order: MutableList<String>,
) : TireScheduleStore {
    override fun find(code: String): TireSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireSchedule) {
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

    override fun list(): MutableList<TireSchedule> {
        val out = mutableListOf<TireSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireScheduleStore(): TireScheduleStore {
    return MemoryTireScheduleStore(mutableMapOf<String, TireSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireScheduleStore(
    var inner: TireScheduleStore,
    var cache: TtlCache<TireSchedule>,
    var hits: Int,
    var misses: Int,
) : TireScheduleStore {
    override fun find(code: String): TireSchedule? {
        val hit = (cache.get(code) as TireSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireSchedule> = inner.list()
}

fun newCachedTireScheduleStore(inner: TireScheduleStore, ttl: Int, clock: Clock): TireScheduleStore {
    return CachedTireScheduleStore(
        inner = inner,
        cache = newTtlCache<TireSchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire schedules. Empty fields match all. */
data class TireScheduleQuery(
    var region: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireSchedule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireSchedule>): MutableList<TireSchedule> {
        val matched = filter(items) { candidate: TireSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireSchedules(store: TireScheduleStore, query: TireScheduleQuery): MutableList<TireSchedule> {
    return query.apply(store.list())
}
