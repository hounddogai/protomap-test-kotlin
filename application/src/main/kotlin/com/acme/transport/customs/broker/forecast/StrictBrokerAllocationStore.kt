package com.acme.transport.customs.broker.forecast

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict broker allocations by code. */
interface StrictBrokerAllocationStore {
    fun find(code: String): StrictBrokerAllocation?
    fun save(item: StrictBrokerAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictBrokerAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictBrokerAllocationStore(
    var items: MutableMap<String, StrictBrokerAllocation>,
    var order: MutableList<String>,
) : StrictBrokerAllocationStore {
    override fun find(code: String): StrictBrokerAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictBrokerAllocation) {
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

    override fun list(): MutableList<StrictBrokerAllocation> {
        val out = mutableListOf<StrictBrokerAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictBrokerAllocationStore(): StrictBrokerAllocationStore {
    return MemoryStrictBrokerAllocationStore(
        mutableMapOf<String, StrictBrokerAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictBrokerAllocationStore(
    var inner: StrictBrokerAllocationStore,
    var cache: TtlCache<StrictBrokerAllocation>,
    var hits: Int,
    var misses: Int,
) : StrictBrokerAllocationStore {
    override fun find(code: String): StrictBrokerAllocation? {
        val hit = (cache.get(code) as StrictBrokerAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictBrokerAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictBrokerAllocation> = inner.list()
}

fun newCachedStrictBrokerAllocationStore(inner: StrictBrokerAllocationStore, ttl: Int, clock: Clock): StrictBrokerAllocationStore {
    return CachedStrictBrokerAllocationStore(
        inner = inner,
        cache = newTtlCache<StrictBrokerAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict broker allocations. Empty fields match all. */
data class StrictBrokerAllocationQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictBrokerAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictBrokerAllocation>): MutableList<StrictBrokerAllocation> {
        val matched = filter(items) { candidate: StrictBrokerAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictBrokerAllocations(store: StrictBrokerAllocationStore, query: StrictBrokerAllocationQuery): MutableList<StrictBrokerAllocation> {
    return query.apply(store.list())
}
