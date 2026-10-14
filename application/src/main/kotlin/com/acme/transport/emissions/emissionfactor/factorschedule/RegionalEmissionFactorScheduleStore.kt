package com.acme.transport.emissions.emissionfactor.factorschedule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional emission factor schedules by code. */
interface RegionalEmissionFactorScheduleStore {
    fun find(code: String): RegionalEmissionFactorSchedule?
    fun save(item: RegionalEmissionFactorSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalEmissionFactorSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalEmissionFactorScheduleStore(
    var items: MutableMap<String, RegionalEmissionFactorSchedule>,
    var order: MutableList<String>,
) : RegionalEmissionFactorScheduleStore {
    override fun find(code: String): RegionalEmissionFactorSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalEmissionFactorSchedule) {
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

    override fun list(): MutableList<RegionalEmissionFactorSchedule> {
        val out = mutableListOf<RegionalEmissionFactorSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalEmissionFactorScheduleStore(): RegionalEmissionFactorScheduleStore {
    return MemoryRegionalEmissionFactorScheduleStore(
        mutableMapOf<String, RegionalEmissionFactorSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalEmissionFactorScheduleStore(
    var inner: RegionalEmissionFactorScheduleStore,
    var cache: TtlCache<RegionalEmissionFactorSchedule>,
    var hits: Int,
    var misses: Int,
) : RegionalEmissionFactorScheduleStore {
    override fun find(code: String): RegionalEmissionFactorSchedule? {
        val hit = (cache.get(code) as RegionalEmissionFactorSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalEmissionFactorSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalEmissionFactorSchedule> = inner.list()
}

fun newCachedRegionalEmissionFactorScheduleStore(inner: RegionalEmissionFactorScheduleStore, ttl: Int, clock: Clock): RegionalEmissionFactorScheduleStore {
    return CachedRegionalEmissionFactorScheduleStore(
        inner = inner,
        cache = newTtlCache<RegionalEmissionFactorSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional emission factor schedules. Empty fields match all. */
data class RegionalEmissionFactorScheduleQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalEmissionFactorSchedule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalEmissionFactorSchedule>): MutableList<RegionalEmissionFactorSchedule> {
        val matched = filter(items) { candidate: RegionalEmissionFactorSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalEmissionFactorSchedules(store: RegionalEmissionFactorScheduleStore, query: RegionalEmissionFactorScheduleQuery): MutableList<RegionalEmissionFactorSchedule> {
    return query.apply(store.list())
}
