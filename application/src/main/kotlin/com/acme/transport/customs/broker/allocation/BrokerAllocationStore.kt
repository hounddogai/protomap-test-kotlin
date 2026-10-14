package com.acme.transport.customs.broker.allocation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker allocations by code. */
interface BrokerAllocationStore {
    fun find(code: String): BrokerAllocation?
    fun save(item: BrokerAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerAllocationStore(
    var items: MutableMap<String, BrokerAllocation>,
    var order: MutableList<String>,
) : BrokerAllocationStore {
    override fun find(code: String): BrokerAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerAllocation) {
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

    override fun list(): MutableList<BrokerAllocation> {
        val out = mutableListOf<BrokerAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerAllocationStore(): BrokerAllocationStore {
    return MemoryBrokerAllocationStore(mutableMapOf<String, BrokerAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerAllocationStore(
    var inner: BrokerAllocationStore,
    var cache: TtlCache<BrokerAllocation>,
    var hits: Int,
    var misses: Int,
) : BrokerAllocationStore {
    override fun find(code: String): BrokerAllocation? {
        val hit = (cache.get(code) as BrokerAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerAllocation> = inner.list()
}

fun newCachedBrokerAllocationStore(inner: BrokerAllocationStore, ttl: Int, clock: Clock): BrokerAllocationStore {
    return CachedBrokerAllocationStore(
        inner = inner,
        cache = newTtlCache<BrokerAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker allocations. Empty fields match all. */
data class BrokerAllocationQuery(
    var segment: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerAllocation): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerAllocation>): MutableList<BrokerAllocation> {
        val matched = filter(items) { candidate: BrokerAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerAllocations(store: BrokerAllocationStore, query: BrokerAllocationQuery): MutableList<BrokerAllocation> {
    return query.apply(store.list())
}
