package com.acme.transport.crewing.availability.profile

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global availability allocations by code. */
interface GlobalAvailabilityAllocationStore {
    fun find(code: String): GlobalAvailabilityAllocation?
    fun save(item: GlobalAvailabilityAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalAvailabilityAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalAvailabilityAllocationStore(
    var items: MutableMap<String, GlobalAvailabilityAllocation>,
    var order: MutableList<String>,
) : GlobalAvailabilityAllocationStore {
    override fun find(code: String): GlobalAvailabilityAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalAvailabilityAllocation) {
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

    override fun list(): MutableList<GlobalAvailabilityAllocation> {
        val out = mutableListOf<GlobalAvailabilityAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalAvailabilityAllocationStore(): GlobalAvailabilityAllocationStore {
    return MemoryGlobalAvailabilityAllocationStore(
        mutableMapOf<String, GlobalAvailabilityAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalAvailabilityAllocationStore(
    var inner: GlobalAvailabilityAllocationStore,
    var cache: TtlCache<GlobalAvailabilityAllocation>,
    var hits: Int,
    var misses: Int,
) : GlobalAvailabilityAllocationStore {
    override fun find(code: String): GlobalAvailabilityAllocation? {
        val hit = (cache.get(code) as GlobalAvailabilityAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalAvailabilityAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalAvailabilityAllocation> = inner.list()
}

fun newCachedGlobalAvailabilityAllocationStore(inner: GlobalAvailabilityAllocationStore, ttl: Int, clock: Clock): GlobalAvailabilityAllocationStore {
    return CachedGlobalAvailabilityAllocationStore(
        inner = inner,
        cache = newTtlCache<GlobalAvailabilityAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global availability allocations. Empty fields match all. */
data class GlobalAvailabilityAllocationQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalAvailabilityAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalAvailabilityAllocation>): MutableList<GlobalAvailabilityAllocation> {
        val matched = filter(items) { candidate: GlobalAvailabilityAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalAvailabilityAllocations(store: GlobalAvailabilityAllocationStore, query: GlobalAvailabilityAllocationQuery): MutableList<GlobalAvailabilityAllocation> {
    return query.apply(store.list())
}
