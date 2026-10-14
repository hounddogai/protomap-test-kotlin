package com.acme.transport.network.zone.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps adaptive zone allocations by code. */
interface AdaptiveZoneAllocationStore {
    fun find(code: String): AdaptiveZoneAllocation?
    fun save(item: AdaptiveZoneAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveZoneAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveZoneAllocationStore(
    var items: MutableMap<String, AdaptiveZoneAllocation>,
    var order: MutableList<String>,
) : AdaptiveZoneAllocationStore {
    override fun find(code: String): AdaptiveZoneAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveZoneAllocation) {
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

    override fun list(): MutableList<AdaptiveZoneAllocation> {
        val out = mutableListOf<AdaptiveZoneAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveZoneAllocationStore(): AdaptiveZoneAllocationStore {
    return MemoryAdaptiveZoneAllocationStore(
        mutableMapOf<String, AdaptiveZoneAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveZoneAllocationStore(
    var inner: AdaptiveZoneAllocationStore,
    var cache: TtlCache<AdaptiveZoneAllocation>,
    var hits: Int,
    var misses: Int,
) : AdaptiveZoneAllocationStore {
    override fun find(code: String): AdaptiveZoneAllocation? {
        val hit = (cache.get(code) as AdaptiveZoneAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveZoneAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveZoneAllocation> = inner.list()
}

fun newCachedAdaptiveZoneAllocationStore(inner: AdaptiveZoneAllocationStore, ttl: Int, clock: Clock): AdaptiveZoneAllocationStore {
    return CachedAdaptiveZoneAllocationStore(
        inner = inner,
        cache = newTtlCache<AdaptiveZoneAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive zone allocations. Empty fields match all. */
data class AdaptiveZoneAllocationQuery(
    var source: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveZoneAllocation): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveZoneAllocation>): MutableList<AdaptiveZoneAllocation> {
        val matched = filter(items) { candidate: AdaptiveZoneAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveZoneAllocations(store: AdaptiveZoneAllocationStore, query: AdaptiveZoneAllocationQuery): MutableList<AdaptiveZoneAllocation> {
    return query.apply(store.list())
}
