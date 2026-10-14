package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive inspection allocations by code. */
interface AdaptiveInspectionAllocationStore {
    fun find(code: String): AdaptiveInspectionAllocation?
    fun save(item: AdaptiveInspectionAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveInspectionAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveInspectionAllocationStore(
    var items: MutableMap<String, AdaptiveInspectionAllocation>,
    var order: MutableList<String>,
) : AdaptiveInspectionAllocationStore {
    override fun find(code: String): AdaptiveInspectionAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveInspectionAllocation) {
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

    override fun list(): MutableList<AdaptiveInspectionAllocation> {
        val out = mutableListOf<AdaptiveInspectionAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveInspectionAllocationStore(): AdaptiveInspectionAllocationStore {
    return MemoryAdaptiveInspectionAllocationStore(
        mutableMapOf<String, AdaptiveInspectionAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveInspectionAllocationStore(
    var inner: AdaptiveInspectionAllocationStore,
    var cache: TtlCache<AdaptiveInspectionAllocation>,
    var hits: Int,
    var misses: Int,
) : AdaptiveInspectionAllocationStore {
    override fun find(code: String): AdaptiveInspectionAllocation? {
        val hit = (cache.get(code) as AdaptiveInspectionAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveInspectionAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveInspectionAllocation> = inner.list()
}

fun newCachedAdaptiveInspectionAllocationStore(inner: AdaptiveInspectionAllocationStore, ttl: Int, clock: Clock): AdaptiveInspectionAllocationStore {
    return CachedAdaptiveInspectionAllocationStore(
        inner = inner,
        cache = newTtlCache<AdaptiveInspectionAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive inspection allocations. Empty fields match all. */
data class AdaptiveInspectionAllocationQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveInspectionAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<AdaptiveInspectionAllocation>): MutableList<AdaptiveInspectionAllocation> {
        val matched = filter(items) { candidate: AdaptiveInspectionAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveInspectionAllocations(store: AdaptiveInspectionAllocationStore, query: AdaptiveInspectionAllocationQuery): MutableList<AdaptiveInspectionAllocation> {
    return query.apply(store.list())
}
