package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability allocations by code. */
interface AvailabilityAllocationStore {
    fun find(code: String): AvailabilityAllocation?
    fun save(item: AvailabilityAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityAllocationStore(
    var items: MutableMap<String, AvailabilityAllocation>,
    var order: MutableList<String>,
) : AvailabilityAllocationStore {
    override fun find(code: String): AvailabilityAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityAllocation) {
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

    override fun list(): MutableList<AvailabilityAllocation> {
        val out = mutableListOf<AvailabilityAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityAllocationStore(): AvailabilityAllocationStore {
    return MemoryAvailabilityAllocationStore(
        mutableMapOf<String, AvailabilityAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityAllocationStore(
    var inner: AvailabilityAllocationStore,
    var cache: TtlCache<AvailabilityAllocation>,
    var hits: Int,
    var misses: Int,
) : AvailabilityAllocationStore {
    override fun find(code: String): AvailabilityAllocation? {
        val hit = (cache.get(code) as AvailabilityAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityAllocation> = inner.list()
}

fun newCachedAvailabilityAllocationStore(inner: AvailabilityAllocationStore, ttl: Int, clock: Clock): AvailabilityAllocationStore {
    return CachedAvailabilityAllocationStore(
        inner = inner,
        cache = newTtlCache<AvailabilityAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability allocations. Empty fields match all. */
data class AvailabilityAllocationQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityAllocation>): MutableList<AvailabilityAllocation> {
        val matched = filter(items) { candidate: AvailabilityAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityAllocations(store: AvailabilityAllocationStore, query: AvailabilityAllocationQuery): MutableList<AvailabilityAllocation> {
    return query.apply(store.list())
}
