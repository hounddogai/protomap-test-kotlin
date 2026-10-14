package com.acme.transport.emissions.fuelburn.schedule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn schedules by code. */
interface FuelBurnScheduleStore {
    fun find(code: String): FuelBurnSchedule?
    fun save(item: FuelBurnSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnScheduleStore(
    var items: MutableMap<String, FuelBurnSchedule>,
    var order: MutableList<String>,
) : FuelBurnScheduleStore {
    override fun find(code: String): FuelBurnSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnSchedule) {
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

    override fun list(): MutableList<FuelBurnSchedule> {
        val out = mutableListOf<FuelBurnSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnScheduleStore(): FuelBurnScheduleStore {
    return MemoryFuelBurnScheduleStore(mutableMapOf<String, FuelBurnSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnScheduleStore(
    var inner: FuelBurnScheduleStore,
    var cache: TtlCache<FuelBurnSchedule>,
    var hits: Int,
    var misses: Int,
) : FuelBurnScheduleStore {
    override fun find(code: String): FuelBurnSchedule? {
        val hit = (cache.get(code) as FuelBurnSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnSchedule> = inner.list()
}

fun newCachedFuelBurnScheduleStore(inner: FuelBurnScheduleStore, ttl: Int, clock: Clock): FuelBurnScheduleStore {
    return CachedFuelBurnScheduleStore(
        inner = inner,
        cache = newTtlCache<FuelBurnSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn schedules. Empty fields match all. */
data class FuelBurnScheduleQuery(
    var unit: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnSchedule): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnSchedule>): MutableList<FuelBurnSchedule> {
        val matched = filter(items) { candidate: FuelBurnSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnSchedules(store: FuelBurnScheduleStore, query: FuelBurnScheduleQuery): MutableList<FuelBurnSchedule> {
    return query.apply(store.list())
}
