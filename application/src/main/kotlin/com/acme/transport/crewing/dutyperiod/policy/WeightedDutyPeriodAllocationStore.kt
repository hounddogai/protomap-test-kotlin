package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted duty period allocations by code. */
interface WeightedDutyPeriodAllocationStore {
    fun find(code: String): WeightedDutyPeriodAllocation?
    fun save(item: WeightedDutyPeriodAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDutyPeriodAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDutyPeriodAllocationStore(
    var items: MutableMap<String, WeightedDutyPeriodAllocation>,
    var order: MutableList<String>,
) : WeightedDutyPeriodAllocationStore {
    override fun find(code: String): WeightedDutyPeriodAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDutyPeriodAllocation) {
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

    override fun list(): MutableList<WeightedDutyPeriodAllocation> {
        val out = mutableListOf<WeightedDutyPeriodAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDutyPeriodAllocationStore(): WeightedDutyPeriodAllocationStore {
    return MemoryWeightedDutyPeriodAllocationStore(
        mutableMapOf<String, WeightedDutyPeriodAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDutyPeriodAllocationStore(
    var inner: WeightedDutyPeriodAllocationStore,
    var cache: TtlCache<WeightedDutyPeriodAllocation>,
    var hits: Int,
    var misses: Int,
) : WeightedDutyPeriodAllocationStore {
    override fun find(code: String): WeightedDutyPeriodAllocation? {
        val hit = (cache.get(code) as WeightedDutyPeriodAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDutyPeriodAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDutyPeriodAllocation> = inner.list()
}

fun newCachedWeightedDutyPeriodAllocationStore(inner: WeightedDutyPeriodAllocationStore, ttl: Int, clock: Clock): WeightedDutyPeriodAllocationStore {
    return CachedWeightedDutyPeriodAllocationStore(
        inner = inner,
        cache = newTtlCache<WeightedDutyPeriodAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted duty period allocations. Empty fields match all. */
data class WeightedDutyPeriodAllocationQuery(
    var segment: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDutyPeriodAllocation): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<WeightedDutyPeriodAllocation>): MutableList<WeightedDutyPeriodAllocation> {
        val matched = filter(items) { candidate: WeightedDutyPeriodAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDutyPeriodAllocations(store: WeightedDutyPeriodAllocationStore, query: WeightedDutyPeriodAllocationQuery): MutableList<WeightedDutyPeriodAllocation> {
    return query.apply(store.list())
}
