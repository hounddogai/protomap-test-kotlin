package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection allocations by code. */
interface InspectionAllocationStore {
    fun find(code: String): InspectionAllocation?
    fun save(item: InspectionAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionAllocationStore(
    var items: MutableMap<String, InspectionAllocation>,
    var order: MutableList<String>,
) : InspectionAllocationStore {
    override fun find(code: String): InspectionAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionAllocation) {
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

    override fun list(): MutableList<InspectionAllocation> {
        val out = mutableListOf<InspectionAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionAllocationStore(): InspectionAllocationStore {
    return MemoryInspectionAllocationStore(
        mutableMapOf<String, InspectionAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionAllocationStore(
    var inner: InspectionAllocationStore,
    var cache: TtlCache<InspectionAllocation>,
    var hits: Int,
    var misses: Int,
) : InspectionAllocationStore {
    override fun find(code: String): InspectionAllocation? {
        val hit = (cache.get(code) as InspectionAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionAllocation> = inner.list()
}

fun newCachedInspectionAllocationStore(inner: InspectionAllocationStore, ttl: Int, clock: Clock): InspectionAllocationStore {
    return CachedInspectionAllocationStore(
        inner = inner,
        cache = newTtlCache<InspectionAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection allocations. Empty fields match all. */
data class InspectionAllocationQuery(
    var channel: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionAllocation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionAllocation>): MutableList<InspectionAllocation> {
        val matched = filter(items) { candidate: InspectionAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionAllocations(store: InspectionAllocationStore, query: InspectionAllocationQuery): MutableList<InspectionAllocation> {
    return query.apply(store.list())
}
