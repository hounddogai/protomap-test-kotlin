package com.acme.transport.lastmile.drop.dropallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global drop allocations by code. */
interface GlobalDropAllocationStore {
    fun find(code: String): GlobalDropAllocation?
    fun save(item: GlobalDropAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDropAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDropAllocationStore(
    var items: MutableMap<String, GlobalDropAllocation>,
    var order: MutableList<String>,
) : GlobalDropAllocationStore {
    override fun find(code: String): GlobalDropAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDropAllocation) {
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

    override fun list(): MutableList<GlobalDropAllocation> {
        val out = mutableListOf<GlobalDropAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDropAllocationStore(): GlobalDropAllocationStore {
    return MemoryGlobalDropAllocationStore(
        mutableMapOf<String, GlobalDropAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDropAllocationStore(
    var inner: GlobalDropAllocationStore,
    var cache: TtlCache<GlobalDropAllocation>,
    var hits: Int,
    var misses: Int,
) : GlobalDropAllocationStore {
    override fun find(code: String): GlobalDropAllocation? {
        val hit = (cache.get(code) as GlobalDropAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDropAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDropAllocation> = inner.list()
}

fun newCachedGlobalDropAllocationStore(inner: GlobalDropAllocationStore, ttl: Int, clock: Clock): GlobalDropAllocationStore {
    return CachedGlobalDropAllocationStore(
        inner = inner,
        cache = newTtlCache<GlobalDropAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global drop allocations. Empty fields match all. */
data class GlobalDropAllocationQuery(
    var country: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDropAllocation): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalDropAllocation>): MutableList<GlobalDropAllocation> {
        val matched = filter(items) { candidate: GlobalDropAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDropAllocations(store: GlobalDropAllocationStore, query: GlobalDropAllocationQuery): MutableList<GlobalDropAllocation> {
    return query.apply(store.list())
}
