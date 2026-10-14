package com.acme.transport.fleetops.defect.cycle

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional defect allocations by code. */
interface RegionalDefectAllocationStore {
    fun find(code: String): RegionalDefectAllocation?
    fun save(item: RegionalDefectAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDefectAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDefectAllocationStore(
    var items: MutableMap<String, RegionalDefectAllocation>,
    var order: MutableList<String>,
) : RegionalDefectAllocationStore {
    override fun find(code: String): RegionalDefectAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDefectAllocation) {
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

    override fun list(): MutableList<RegionalDefectAllocation> {
        val out = mutableListOf<RegionalDefectAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDefectAllocationStore(): RegionalDefectAllocationStore {
    return MemoryRegionalDefectAllocationStore(
        mutableMapOf<String, RegionalDefectAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDefectAllocationStore(
    var inner: RegionalDefectAllocationStore,
    var cache: TtlCache<RegionalDefectAllocation>,
    var hits: Int,
    var misses: Int,
) : RegionalDefectAllocationStore {
    override fun find(code: String): RegionalDefectAllocation? {
        val hit = (cache.get(code) as RegionalDefectAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDefectAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDefectAllocation> = inner.list()
}

fun newCachedRegionalDefectAllocationStore(inner: RegionalDefectAllocationStore, ttl: Int, clock: Clock): RegionalDefectAllocationStore {
    return CachedRegionalDefectAllocationStore(
        inner = inner,
        cache = newTtlCache<RegionalDefectAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional defect allocations. Empty fields match all. */
data class RegionalDefectAllocationQuery(
    var country: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDefectAllocation): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<RegionalDefectAllocation>): MutableList<RegionalDefectAllocation> {
        val matched = filter(items) { candidate: RegionalDefectAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDefectAllocations(store: RegionalDefectAllocationStore, query: RegionalDefectAllocationQuery): MutableList<RegionalDefectAllocation> {
    return query.apply(store.list())
}
