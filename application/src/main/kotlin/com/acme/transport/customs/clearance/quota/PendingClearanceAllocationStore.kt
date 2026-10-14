package com.acme.transport.customs.clearance.quota

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending clearance allocations by code. */
interface PendingClearanceAllocationStore {
    fun find(code: String): PendingClearanceAllocation?
    fun save(item: PendingClearanceAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingClearanceAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingClearanceAllocationStore(
    var items: MutableMap<String, PendingClearanceAllocation>,
    var order: MutableList<String>,
) : PendingClearanceAllocationStore {
    override fun find(code: String): PendingClearanceAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingClearanceAllocation) {
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

    override fun list(): MutableList<PendingClearanceAllocation> {
        val out = mutableListOf<PendingClearanceAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingClearanceAllocationStore(): PendingClearanceAllocationStore {
    return MemoryPendingClearanceAllocationStore(
        mutableMapOf<String, PendingClearanceAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingClearanceAllocationStore(
    var inner: PendingClearanceAllocationStore,
    var cache: TtlCache<PendingClearanceAllocation>,
    var hits: Int,
    var misses: Int,
) : PendingClearanceAllocationStore {
    override fun find(code: String): PendingClearanceAllocation? {
        val hit = (cache.get(code) as PendingClearanceAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingClearanceAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingClearanceAllocation> = inner.list()
}

fun newCachedPendingClearanceAllocationStore(inner: PendingClearanceAllocationStore, ttl: Int, clock: Clock): PendingClearanceAllocationStore {
    return CachedPendingClearanceAllocationStore(
        inner = inner,
        cache = newTtlCache<PendingClearanceAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending clearance allocations. Empty fields match all. */
data class PendingClearanceAllocationQuery(
    var channel: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingClearanceAllocation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingClearanceAllocation>): MutableList<PendingClearanceAllocation> {
        val matched = filter(items) { candidate: PendingClearanceAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingClearanceAllocations(store: PendingClearanceAllocationStore, query: PendingClearanceAllocationQuery): MutableList<PendingClearanceAllocation> {
    return query.apply(store.list())
}
