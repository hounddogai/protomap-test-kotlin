package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub allocations by code. */
interface HubAllocationStore {
    fun find(code: String): HubAllocation?
    fun save(item: HubAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubAllocationStore(
    var items: MutableMap<String, HubAllocation>,
    var order: MutableList<String>,
) : HubAllocationStore {
    override fun find(code: String): HubAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubAllocation) {
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

    override fun list(): MutableList<HubAllocation> {
        val out = mutableListOf<HubAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubAllocationStore(): HubAllocationStore {
    return MemoryHubAllocationStore(mutableMapOf<String, HubAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubAllocationStore(
    var inner: HubAllocationStore,
    var cache: TtlCache<HubAllocation>,
    var hits: Int,
    var misses: Int,
) : HubAllocationStore {
    override fun find(code: String): HubAllocation? {
        val hit = (cache.get(code) as HubAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubAllocation> = inner.list()
}

fun newCachedHubAllocationStore(inner: HubAllocationStore, ttl: Int, clock: Clock): HubAllocationStore {
    return CachedHubAllocationStore(
        inner = inner,
        cache = newTtlCache<HubAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub allocations. Empty fields match all. */
data class HubAllocationQuery(
    var region: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubAllocation): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<HubAllocation>): MutableList<HubAllocation> {
        val matched = filter(items) { candidate: HubAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubAllocations(store: HubAllocationStore, query: HubAllocationQuery): MutableList<HubAllocation> {
    return query.apply(store.list())
}
