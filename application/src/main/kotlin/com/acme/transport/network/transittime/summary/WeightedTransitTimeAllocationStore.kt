package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted transit time allocations by code. */
interface WeightedTransitTimeAllocationStore {
    fun find(code: String): WeightedTransitTimeAllocation?
    fun save(item: WeightedTransitTimeAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTransitTimeAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTransitTimeAllocationStore(
    var items: MutableMap<String, WeightedTransitTimeAllocation>,
    var order: MutableList<String>,
) : WeightedTransitTimeAllocationStore {
    override fun find(code: String): WeightedTransitTimeAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTransitTimeAllocation) {
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

    override fun list(): MutableList<WeightedTransitTimeAllocation> {
        val out = mutableListOf<WeightedTransitTimeAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTransitTimeAllocationStore(): WeightedTransitTimeAllocationStore {
    return MemoryWeightedTransitTimeAllocationStore(
        mutableMapOf<String, WeightedTransitTimeAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTransitTimeAllocationStore(
    var inner: WeightedTransitTimeAllocationStore,
    var cache: TtlCache<WeightedTransitTimeAllocation>,
    var hits: Int,
    var misses: Int,
) : WeightedTransitTimeAllocationStore {
    override fun find(code: String): WeightedTransitTimeAllocation? {
        val hit = (cache.get(code) as WeightedTransitTimeAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTransitTimeAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTransitTimeAllocation> = inner.list()
}

fun newCachedWeightedTransitTimeAllocationStore(inner: WeightedTransitTimeAllocationStore, ttl: Int, clock: Clock): WeightedTransitTimeAllocationStore {
    return CachedWeightedTransitTimeAllocationStore(
        inner = inner,
        cache = newTtlCache<WeightedTransitTimeAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted transit time allocations. Empty fields match all. */
data class WeightedTransitTimeAllocationQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTransitTimeAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTransitTimeAllocation>): MutableList<WeightedTransitTimeAllocation> {
        val matched = filter(items) { candidate: WeightedTransitTimeAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTransitTimeAllocations(store: WeightedTransitTimeAllocationStore, query: WeightedTransitTimeAllocationQuery): MutableList<WeightedTransitTimeAllocation> {
    return query.apply(store.list())
}
