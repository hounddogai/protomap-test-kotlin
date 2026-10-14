package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict duty period allocations by code. */
interface StrictDutyPeriodAllocationStore {
    fun find(code: String): StrictDutyPeriodAllocation?
    fun save(item: StrictDutyPeriodAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDutyPeriodAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDutyPeriodAllocationStore(
    var items: MutableMap<String, StrictDutyPeriodAllocation>,
    var order: MutableList<String>,
) : StrictDutyPeriodAllocationStore {
    override fun find(code: String): StrictDutyPeriodAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDutyPeriodAllocation) {
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

    override fun list(): MutableList<StrictDutyPeriodAllocation> {
        val out = mutableListOf<StrictDutyPeriodAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDutyPeriodAllocationStore(): StrictDutyPeriodAllocationStore {
    return MemoryStrictDutyPeriodAllocationStore(
        mutableMapOf<String, StrictDutyPeriodAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDutyPeriodAllocationStore(
    var inner: StrictDutyPeriodAllocationStore,
    var cache: TtlCache<StrictDutyPeriodAllocation>,
    var hits: Int,
    var misses: Int,
) : StrictDutyPeriodAllocationStore {
    override fun find(code: String): StrictDutyPeriodAllocation? {
        val hit = (cache.get(code) as StrictDutyPeriodAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDutyPeriodAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDutyPeriodAllocation> = inner.list()
}

fun newCachedStrictDutyPeriodAllocationStore(inner: StrictDutyPeriodAllocationStore, ttl: Int, clock: Clock): StrictDutyPeriodAllocationStore {
    return CachedStrictDutyPeriodAllocationStore(
        inner = inner,
        cache = newTtlCache<StrictDutyPeriodAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict duty period allocations. Empty fields match all. */
data class StrictDutyPeriodAllocationQuery(
    var depot: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDutyPeriodAllocation): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDutyPeriodAllocation>): MutableList<StrictDutyPeriodAllocation> {
        val matched = filter(items) { candidate: StrictDutyPeriodAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDutyPeriodAllocations(store: StrictDutyPeriodAllocationStore, query: StrictDutyPeriodAllocationQuery): MutableList<StrictDutyPeriodAllocation> {
    return query.apply(store.list())
}
