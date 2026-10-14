package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional drop allocations by code. */
interface RegionalDropAllocationStore {
    fun find(code: String): RegionalDropAllocation?
    fun save(item: RegionalDropAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDropAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDropAllocationStore(
    var items: MutableMap<String, RegionalDropAllocation>,
    var order: MutableList<String>,
) : RegionalDropAllocationStore {
    override fun find(code: String): RegionalDropAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDropAllocation) {
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

    override fun list(): MutableList<RegionalDropAllocation> {
        val out = mutableListOf<RegionalDropAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDropAllocationStore(): RegionalDropAllocationStore {
    return MemoryRegionalDropAllocationStore(
        mutableMapOf<String, RegionalDropAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDropAllocationStore(
    var inner: RegionalDropAllocationStore,
    var cache: TtlCache<RegionalDropAllocation>,
    var hits: Int,
    var misses: Int,
) : RegionalDropAllocationStore {
    override fun find(code: String): RegionalDropAllocation? {
        val hit = (cache.get(code) as RegionalDropAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDropAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDropAllocation> = inner.list()
}

fun newCachedRegionalDropAllocationStore(inner: RegionalDropAllocationStore, ttl: Int, clock: Clock): RegionalDropAllocationStore {
    return CachedRegionalDropAllocationStore(
        inner = inner,
        cache = newTtlCache<RegionalDropAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional drop allocations. Empty fields match all. */
data class RegionalDropAllocationQuery(
    var depot: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDropAllocation): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDropAllocation>): MutableList<RegionalDropAllocation> {
        val matched = filter(items) { candidate: RegionalDropAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDropAllocations(store: RegionalDropAllocationStore, query: RegionalDropAllocationQuery): MutableList<RegionalDropAllocation> {
    return query.apply(store.list())
}
