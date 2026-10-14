package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted inspection allocations by code. */
interface WeightedInspectionAllocationStore {
    fun find(code: String): WeightedInspectionAllocation?
    fun save(item: WeightedInspectionAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedInspectionAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedInspectionAllocationStore(
    var items: MutableMap<String, WeightedInspectionAllocation>,
    var order: MutableList<String>,
) : WeightedInspectionAllocationStore {
    override fun find(code: String): WeightedInspectionAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedInspectionAllocation) {
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

    override fun list(): MutableList<WeightedInspectionAllocation> {
        val out = mutableListOf<WeightedInspectionAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedInspectionAllocationStore(): WeightedInspectionAllocationStore {
    return MemoryWeightedInspectionAllocationStore(
        mutableMapOf<String, WeightedInspectionAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedInspectionAllocationStore(
    var inner: WeightedInspectionAllocationStore,
    var cache: TtlCache<WeightedInspectionAllocation>,
    var hits: Int,
    var misses: Int,
) : WeightedInspectionAllocationStore {
    override fun find(code: String): WeightedInspectionAllocation? {
        val hit = (cache.get(code) as WeightedInspectionAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedInspectionAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedInspectionAllocation> = inner.list()
}

fun newCachedWeightedInspectionAllocationStore(inner: WeightedInspectionAllocationStore, ttl: Int, clock: Clock): WeightedInspectionAllocationStore {
    return CachedWeightedInspectionAllocationStore(
        inner = inner,
        cache = newTtlCache<WeightedInspectionAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted inspection allocations. Empty fields match all. */
data class WeightedInspectionAllocationQuery(
    var source: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedInspectionAllocation): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedInspectionAllocation>): MutableList<WeightedInspectionAllocation> {
        val matched = filter(items) { candidate: WeightedInspectionAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedInspectionAllocations(store: WeightedInspectionAllocationStore, query: WeightedInspectionAllocationQuery): MutableList<WeightedInspectionAllocation> {
    return query.apply(store.list())
}
