package com.acme.transport.crewing.swap.assignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive swap allocations by code. */
interface AdaptiveSwapAllocationStore {
    fun find(code: String): AdaptiveSwapAllocation?
    fun save(item: AdaptiveSwapAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveSwapAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveSwapAllocationStore(
    var items: MutableMap<String, AdaptiveSwapAllocation>,
    var order: MutableList<String>,
) : AdaptiveSwapAllocationStore {
    override fun find(code: String): AdaptiveSwapAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveSwapAllocation) {
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

    override fun list(): MutableList<AdaptiveSwapAllocation> {
        val out = mutableListOf<AdaptiveSwapAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveSwapAllocationStore(): AdaptiveSwapAllocationStore {
    return MemoryAdaptiveSwapAllocationStore(
        mutableMapOf<String, AdaptiveSwapAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveSwapAllocationStore(
    var inner: AdaptiveSwapAllocationStore,
    var cache: TtlCache<AdaptiveSwapAllocation>,
    var hits: Int,
    var misses: Int,
) : AdaptiveSwapAllocationStore {
    override fun find(code: String): AdaptiveSwapAllocation? {
        val hit = (cache.get(code) as AdaptiveSwapAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveSwapAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveSwapAllocation> = inner.list()
}

fun newCachedAdaptiveSwapAllocationStore(inner: AdaptiveSwapAllocationStore, ttl: Int, clock: Clock): AdaptiveSwapAllocationStore {
    return CachedAdaptiveSwapAllocationStore(
        inner = inner,
        cache = newTtlCache<AdaptiveSwapAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive swap allocations. Empty fields match all. */
data class AdaptiveSwapAllocationQuery(
    var country: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveSwapAllocation): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveSwapAllocation>): MutableList<AdaptiveSwapAllocation> {
        val matched = filter(items) { candidate: AdaptiveSwapAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveSwapAllocations(store: AdaptiveSwapAllocationStore, query: AdaptiveSwapAllocationQuery): MutableList<AdaptiveSwapAllocation> {
    return query.apply(store.list())
}
