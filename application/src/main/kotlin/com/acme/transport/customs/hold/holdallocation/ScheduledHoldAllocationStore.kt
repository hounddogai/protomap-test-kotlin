package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled hold allocations by code. */
interface ScheduledHoldAllocationStore {
    fun find(code: String): ScheduledHoldAllocation?
    fun save(item: ScheduledHoldAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledHoldAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledHoldAllocationStore(
    var items: MutableMap<String, ScheduledHoldAllocation>,
    var order: MutableList<String>,
) : ScheduledHoldAllocationStore {
    override fun find(code: String): ScheduledHoldAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledHoldAllocation) {
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

    override fun list(): MutableList<ScheduledHoldAllocation> {
        val out = mutableListOf<ScheduledHoldAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledHoldAllocationStore(): ScheduledHoldAllocationStore {
    return MemoryScheduledHoldAllocationStore(
        mutableMapOf<String, ScheduledHoldAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledHoldAllocationStore(
    var inner: ScheduledHoldAllocationStore,
    var cache: TtlCache<ScheduledHoldAllocation>,
    var hits: Int,
    var misses: Int,
) : ScheduledHoldAllocationStore {
    override fun find(code: String): ScheduledHoldAllocation? {
        val hit = (cache.get(code) as ScheduledHoldAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledHoldAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledHoldAllocation> = inner.list()
}

fun newCachedScheduledHoldAllocationStore(inner: ScheduledHoldAllocationStore, ttl: Int, clock: Clock): ScheduledHoldAllocationStore {
    return CachedScheduledHoldAllocationStore(
        inner = inner,
        cache = newTtlCache<ScheduledHoldAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled hold allocations. Empty fields match all. */
data class ScheduledHoldAllocationQuery(
    var unit: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledHoldAllocation): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledHoldAllocation>): MutableList<ScheduledHoldAllocation> {
        val matched = filter(items) { candidate: ScheduledHoldAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledHoldAllocations(store: ScheduledHoldAllocationStore, query: ScheduledHoldAllocationQuery): MutableList<ScheduledHoldAllocation> {
    return query.apply(store.list())
}
