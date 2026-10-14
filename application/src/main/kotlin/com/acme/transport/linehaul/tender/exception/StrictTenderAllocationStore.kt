package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict tender allocations by code. */
interface StrictTenderAllocationStore {
    fun find(code: String): StrictTenderAllocation?
    fun save(item: StrictTenderAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTenderAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTenderAllocationStore(
    var items: MutableMap<String, StrictTenderAllocation>,
    var order: MutableList<String>,
) : StrictTenderAllocationStore {
    override fun find(code: String): StrictTenderAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTenderAllocation) {
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

    override fun list(): MutableList<StrictTenderAllocation> {
        val out = mutableListOf<StrictTenderAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTenderAllocationStore(): StrictTenderAllocationStore {
    return MemoryStrictTenderAllocationStore(
        mutableMapOf<String, StrictTenderAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTenderAllocationStore(
    var inner: StrictTenderAllocationStore,
    var cache: TtlCache<StrictTenderAllocation>,
    var hits: Int,
    var misses: Int,
) : StrictTenderAllocationStore {
    override fun find(code: String): StrictTenderAllocation? {
        val hit = (cache.get(code) as StrictTenderAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTenderAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTenderAllocation> = inner.list()
}

fun newCachedStrictTenderAllocationStore(inner: StrictTenderAllocationStore, ttl: Int, clock: Clock): StrictTenderAllocationStore {
    return CachedStrictTenderAllocationStore(
        inner = inner,
        cache = newTtlCache<StrictTenderAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict tender allocations. Empty fields match all. */
data class StrictTenderAllocationQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTenderAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTenderAllocation>): MutableList<StrictTenderAllocation> {
        val matched = filter(items) { candidate: StrictTenderAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTenderAllocations(store: StrictTenderAllocationStore, query: StrictTenderAllocationQuery): MutableList<StrictTenderAllocation> {
    return query.apply(store.list())
}
