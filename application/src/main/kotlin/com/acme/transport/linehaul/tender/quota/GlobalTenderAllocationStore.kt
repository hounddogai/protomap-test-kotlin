package com.acme.transport.linehaul.tender.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global tender allocations by code. */
interface GlobalTenderAllocationStore {
    fun find(code: String): GlobalTenderAllocation?
    fun save(item: GlobalTenderAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTenderAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTenderAllocationStore(
    var items: MutableMap<String, GlobalTenderAllocation>,
    var order: MutableList<String>,
) : GlobalTenderAllocationStore {
    override fun find(code: String): GlobalTenderAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTenderAllocation) {
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

    override fun list(): MutableList<GlobalTenderAllocation> {
        val out = mutableListOf<GlobalTenderAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTenderAllocationStore(): GlobalTenderAllocationStore {
    return MemoryGlobalTenderAllocationStore(
        mutableMapOf<String, GlobalTenderAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTenderAllocationStore(
    var inner: GlobalTenderAllocationStore,
    var cache: TtlCache<GlobalTenderAllocation>,
    var hits: Int,
    var misses: Int,
) : GlobalTenderAllocationStore {
    override fun find(code: String): GlobalTenderAllocation? {
        val hit = (cache.get(code) as GlobalTenderAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTenderAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTenderAllocation> = inner.list()
}

fun newCachedGlobalTenderAllocationStore(inner: GlobalTenderAllocationStore, ttl: Int, clock: Clock): GlobalTenderAllocationStore {
    return CachedGlobalTenderAllocationStore(
        inner = inner,
        cache = newTtlCache<GlobalTenderAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global tender allocations. Empty fields match all. */
data class GlobalTenderAllocationQuery(
    var depot: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTenderAllocation): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalTenderAllocation>): MutableList<GlobalTenderAllocation> {
        val matched = filter(items) { candidate: GlobalTenderAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTenderAllocations(store: GlobalTenderAllocationStore, query: GlobalTenderAllocationQuery): MutableList<GlobalTenderAllocation> {
    return query.apply(store.list())
}
