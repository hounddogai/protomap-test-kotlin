package com.acme.transport.terminal.traileryard.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted trailer yard allocations by code. */
interface WeightedTrailerYardAllocationStore {
    fun find(code: String): WeightedTrailerYardAllocation?
    fun save(item: WeightedTrailerYardAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTrailerYardAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTrailerYardAllocationStore(
    var items: MutableMap<String, WeightedTrailerYardAllocation>,
    var order: MutableList<String>,
) : WeightedTrailerYardAllocationStore {
    override fun find(code: String): WeightedTrailerYardAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTrailerYardAllocation) {
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

    override fun list(): MutableList<WeightedTrailerYardAllocation> {
        val out = mutableListOf<WeightedTrailerYardAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTrailerYardAllocationStore(): WeightedTrailerYardAllocationStore {
    return MemoryWeightedTrailerYardAllocationStore(
        mutableMapOf<String, WeightedTrailerYardAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTrailerYardAllocationStore(
    var inner: WeightedTrailerYardAllocationStore,
    var cache: TtlCache<WeightedTrailerYardAllocation>,
    var hits: Int,
    var misses: Int,
) : WeightedTrailerYardAllocationStore {
    override fun find(code: String): WeightedTrailerYardAllocation? {
        val hit = (cache.get(code) as WeightedTrailerYardAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTrailerYardAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTrailerYardAllocation> = inner.list()
}

fun newCachedWeightedTrailerYardAllocationStore(inner: WeightedTrailerYardAllocationStore, ttl: Int, clock: Clock): WeightedTrailerYardAllocationStore {
    return CachedWeightedTrailerYardAllocationStore(
        inner = inner,
        cache = newTtlCache<WeightedTrailerYardAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted trailer yard allocations. Empty fields match all. */
data class WeightedTrailerYardAllocationQuery(
    var region: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTrailerYardAllocation): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTrailerYardAllocation>): MutableList<WeightedTrailerYardAllocation> {
        val matched = filter(items) { candidate: WeightedTrailerYardAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTrailerYardAllocations(store: WeightedTrailerYardAllocationStore, query: WeightedTrailerYardAllocationQuery): MutableList<WeightedTrailerYardAllocation> {
    return query.apply(store.list())
}
