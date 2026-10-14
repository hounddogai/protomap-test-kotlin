package com.acme.transport.customs.hold.allocation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold allocations by code. */
interface HoldAllocationStore {
    fun find(code: String): HoldAllocation?
    fun save(item: HoldAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldAllocationStore(
    var items: MutableMap<String, HoldAllocation>,
    var order: MutableList<String>,
) : HoldAllocationStore {
    override fun find(code: String): HoldAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldAllocation) {
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

    override fun list(): MutableList<HoldAllocation> {
        val out = mutableListOf<HoldAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldAllocationStore(): HoldAllocationStore {
    return MemoryHoldAllocationStore(mutableMapOf<String, HoldAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldAllocationStore(
    var inner: HoldAllocationStore,
    var cache: TtlCache<HoldAllocation>,
    var hits: Int,
    var misses: Int,
) : HoldAllocationStore {
    override fun find(code: String): HoldAllocation? {
        val hit = (cache.get(code) as HoldAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldAllocation> = inner.list()
}

fun newCachedHoldAllocationStore(inner: HoldAllocationStore, ttl: Int, clock: Clock): HoldAllocationStore {
    return CachedHoldAllocationStore(
        inner = inner,
        cache = newTtlCache<HoldAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold allocations. Empty fields match all. */
data class HoldAllocationQuery(
    var lane: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldAllocation): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldAllocation>): MutableList<HoldAllocation> {
        val matched = filter(items) { candidate: HoldAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldAllocations(store: HoldAllocationStore, query: HoldAllocationQuery): MutableList<HoldAllocation> {
    return query.apply(store.list())
}
