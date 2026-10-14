package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled capacity allocations by code. */
interface ScheduledCapacityAllocationStore {
    fun find(code: String): ScheduledCapacityAllocation?
    fun save(item: ScheduledCapacityAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledCapacityAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledCapacityAllocationStore(
    var items: MutableMap<String, ScheduledCapacityAllocation>,
    var order: MutableList<String>,
) : ScheduledCapacityAllocationStore {
    override fun find(code: String): ScheduledCapacityAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledCapacityAllocation) {
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

    override fun list(): MutableList<ScheduledCapacityAllocation> {
        val out = mutableListOf<ScheduledCapacityAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledCapacityAllocationStore(): ScheduledCapacityAllocationStore {
    return MemoryScheduledCapacityAllocationStore(
        mutableMapOf<String, ScheduledCapacityAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledCapacityAllocationStore(
    var inner: ScheduledCapacityAllocationStore,
    var cache: TtlCache<ScheduledCapacityAllocation>,
    var hits: Int,
    var misses: Int,
) : ScheduledCapacityAllocationStore {
    override fun find(code: String): ScheduledCapacityAllocation? {
        val hit = (cache.get(code) as ScheduledCapacityAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledCapacityAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledCapacityAllocation> = inner.list()
}

fun newCachedScheduledCapacityAllocationStore(inner: ScheduledCapacityAllocationStore, ttl: Int, clock: Clock): ScheduledCapacityAllocationStore {
    return CachedScheduledCapacityAllocationStore(
        inner = inner,
        cache = newTtlCache<ScheduledCapacityAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled capacity allocations. Empty fields match all. */
data class ScheduledCapacityAllocationQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledCapacityAllocation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ScheduledCapacityAllocation>): MutableList<ScheduledCapacityAllocation> {
        val matched = filter(items) { candidate: ScheduledCapacityAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledCapacityAllocations(store: ScheduledCapacityAllocationStore, query: ScheduledCapacityAllocationQuery): MutableList<ScheduledCapacityAllocation> {
    return query.apply(store.list())
}
