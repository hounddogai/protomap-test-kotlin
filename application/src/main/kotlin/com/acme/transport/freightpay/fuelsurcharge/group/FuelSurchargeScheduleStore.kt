package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge schedules by code. */
interface FuelSurchargeScheduleStore {
    fun find(code: String): FuelSurchargeSchedule?
    fun save(item: FuelSurchargeSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeScheduleStore(
    var items: MutableMap<String, FuelSurchargeSchedule>,
    var order: MutableList<String>,
) : FuelSurchargeScheduleStore {
    override fun find(code: String): FuelSurchargeSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeSchedule) {
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

    override fun list(): MutableList<FuelSurchargeSchedule> {
        val out = mutableListOf<FuelSurchargeSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeScheduleStore(): FuelSurchargeScheduleStore {
    return MemoryFuelSurchargeScheduleStore(
        mutableMapOf<String, FuelSurchargeSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeScheduleStore(
    var inner: FuelSurchargeScheduleStore,
    var cache: TtlCache<FuelSurchargeSchedule>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeScheduleStore {
    override fun find(code: String): FuelSurchargeSchedule? {
        val hit = (cache.get(code) as FuelSurchargeSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeSchedule> = inner.list()
}

fun newCachedFuelSurchargeScheduleStore(inner: FuelSurchargeScheduleStore, ttl: Int, clock: Clock): FuelSurchargeScheduleStore {
    return CachedFuelSurchargeScheduleStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge schedules. Empty fields match all. */
data class FuelSurchargeScheduleQuery(
    var locale: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeSchedule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeSchedule>): MutableList<FuelSurchargeSchedule> {
        val matched = filter(items) { candidate: FuelSurchargeSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeSchedules(store: FuelSurchargeScheduleStore, query: FuelSurchargeScheduleQuery): MutableList<FuelSurchargeSchedule> {
    return query.apply(store.list())
}
