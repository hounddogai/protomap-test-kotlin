package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period allocations by code. */
interface DutyPeriodAllocationStore {
    fun find(code: String): DutyPeriodAllocation?
    fun save(item: DutyPeriodAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodAllocationStore(
    var items: MutableMap<String, DutyPeriodAllocation>,
    var order: MutableList<String>,
) : DutyPeriodAllocationStore {
    override fun find(code: String): DutyPeriodAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodAllocation) {
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

    override fun list(): MutableList<DutyPeriodAllocation> {
        val out = mutableListOf<DutyPeriodAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodAllocationStore(): DutyPeriodAllocationStore {
    return MemoryDutyPeriodAllocationStore(
        mutableMapOf<String, DutyPeriodAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodAllocationStore(
    var inner: DutyPeriodAllocationStore,
    var cache: TtlCache<DutyPeriodAllocation>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodAllocationStore {
    override fun find(code: String): DutyPeriodAllocation? {
        val hit = (cache.get(code) as DutyPeriodAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodAllocation> = inner.list()
}

fun newCachedDutyPeriodAllocationStore(inner: DutyPeriodAllocationStore, ttl: Int, clock: Clock): DutyPeriodAllocationStore {
    return CachedDutyPeriodAllocationStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period allocations. Empty fields match all. */
data class DutyPeriodAllocationQuery(
    var category: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodAllocation): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodAllocation>): MutableList<DutyPeriodAllocation> {
        val matched = filter(items) { candidate: DutyPeriodAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodAllocations(store: DutyPeriodAllocationStore, query: DutyPeriodAllocationQuery): MutableList<DutyPeriodAllocation> {
    return query.apply(store.list())
}
