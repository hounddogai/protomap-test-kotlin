package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance allocations by code. */
interface ClearanceAllocationStore {
    fun find(code: String): ClearanceAllocation?
    fun save(item: ClearanceAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceAllocationStore(
    var items: MutableMap<String, ClearanceAllocation>,
    var order: MutableList<String>,
) : ClearanceAllocationStore {
    override fun find(code: String): ClearanceAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceAllocation) {
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

    override fun list(): MutableList<ClearanceAllocation> {
        val out = mutableListOf<ClearanceAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceAllocationStore(): ClearanceAllocationStore {
    return MemoryClearanceAllocationStore(mutableMapOf<String, ClearanceAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceAllocationStore(
    var inner: ClearanceAllocationStore,
    var cache: TtlCache<ClearanceAllocation>,
    var hits: Int,
    var misses: Int,
) : ClearanceAllocationStore {
    override fun find(code: String): ClearanceAllocation? {
        val hit = (cache.get(code) as ClearanceAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceAllocation> = inner.list()
}

fun newCachedClearanceAllocationStore(inner: ClearanceAllocationStore, ttl: Int, clock: Clock): ClearanceAllocationStore {
    return CachedClearanceAllocationStore(
        inner = inner,
        cache = newTtlCache<ClearanceAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance allocations. Empty fields match all. */
data class ClearanceAllocationQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceAllocation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ClearanceAllocation>): MutableList<ClearanceAllocation> {
        val matched = filter(items) { candidate: ClearanceAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceAllocations(store: ClearanceAllocationStore, query: ClearanceAllocationQuery): MutableList<ClearanceAllocation> {
    return query.apply(store.list())
}
