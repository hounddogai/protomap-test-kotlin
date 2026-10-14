package com.acme.transport.network.node.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps adaptive node allocations by code. */
interface AdaptiveNodeAllocationStore {
    fun find(code: String): AdaptiveNodeAllocation?
    fun save(item: AdaptiveNodeAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveNodeAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveNodeAllocationStore(
    var items: MutableMap<String, AdaptiveNodeAllocation>,
    var order: MutableList<String>,
) : AdaptiveNodeAllocationStore {
    override fun find(code: String): AdaptiveNodeAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveNodeAllocation) {
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

    override fun list(): MutableList<AdaptiveNodeAllocation> {
        val out = mutableListOf<AdaptiveNodeAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveNodeAllocationStore(): AdaptiveNodeAllocationStore {
    return MemoryAdaptiveNodeAllocationStore(
        mutableMapOf<String, AdaptiveNodeAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveNodeAllocationStore(
    var inner: AdaptiveNodeAllocationStore,
    var cache: TtlCache<AdaptiveNodeAllocation>,
    var hits: Int,
    var misses: Int,
) : AdaptiveNodeAllocationStore {
    override fun find(code: String): AdaptiveNodeAllocation? {
        val hit = (cache.get(code) as AdaptiveNodeAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveNodeAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveNodeAllocation> = inner.list()
}

fun newCachedAdaptiveNodeAllocationStore(inner: AdaptiveNodeAllocationStore, ttl: Int, clock: Clock): AdaptiveNodeAllocationStore {
    return CachedAdaptiveNodeAllocationStore(
        inner = inner,
        cache = newTtlCache<AdaptiveNodeAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive node allocations. Empty fields match all. */
data class AdaptiveNodeAllocationQuery(
    var category: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveNodeAllocation): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveNodeAllocation>): MutableList<AdaptiveNodeAllocation> {
        val matched = filter(items) { candidate: AdaptiveNodeAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveNodeAllocations(store: AdaptiveNodeAllocationStore, query: AdaptiveNodeAllocationQuery): MutableList<AdaptiveNodeAllocation> {
    return query.apply(store.list())
}
