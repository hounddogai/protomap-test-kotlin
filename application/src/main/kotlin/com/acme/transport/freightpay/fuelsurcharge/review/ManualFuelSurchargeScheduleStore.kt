package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual fuel surcharge schedules by code. */
interface ManualFuelSurchargeScheduleStore {
    fun find(code: String): ManualFuelSurchargeSchedule?
    fun save(item: ManualFuelSurchargeSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualFuelSurchargeSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualFuelSurchargeScheduleStore(
    var items: MutableMap<String, ManualFuelSurchargeSchedule>,
    var order: MutableList<String>,
) : ManualFuelSurchargeScheduleStore {
    override fun find(code: String): ManualFuelSurchargeSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualFuelSurchargeSchedule) {
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

    override fun list(): MutableList<ManualFuelSurchargeSchedule> {
        val out = mutableListOf<ManualFuelSurchargeSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualFuelSurchargeScheduleStore(): ManualFuelSurchargeScheduleStore {
    return MemoryManualFuelSurchargeScheduleStore(
        mutableMapOf<String, ManualFuelSurchargeSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualFuelSurchargeScheduleStore(
    var inner: ManualFuelSurchargeScheduleStore,
    var cache: TtlCache<ManualFuelSurchargeSchedule>,
    var hits: Int,
    var misses: Int,
) : ManualFuelSurchargeScheduleStore {
    override fun find(code: String): ManualFuelSurchargeSchedule? {
        val hit = (cache.get(code) as ManualFuelSurchargeSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualFuelSurchargeSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualFuelSurchargeSchedule> = inner.list()
}

fun newCachedManualFuelSurchargeScheduleStore(inner: ManualFuelSurchargeScheduleStore, ttl: Int, clock: Clock): ManualFuelSurchargeScheduleStore {
    return CachedManualFuelSurchargeScheduleStore(
        inner = inner,
        cache = newTtlCache<ManualFuelSurchargeSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual fuel surcharge schedules. Empty fields match all. */
data class ManualFuelSurchargeScheduleQuery(
    var region: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualFuelSurchargeSchedule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualFuelSurchargeSchedule>): MutableList<ManualFuelSurchargeSchedule> {
        val matched = filter(items) { candidate: ManualFuelSurchargeSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualFuelSurchargeSchedules(store: ManualFuelSurchargeScheduleStore, query: ManualFuelSurchargeScheduleQuery): MutableList<ManualFuelSurchargeSchedule> {
    return query.apply(store.list())
}
