package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge allocations by code. */
interface FuelSurchargeAllocationStore {
    fun find(code: String): FuelSurchargeAllocation?
    fun save(item: FuelSurchargeAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeAllocationStore(
    var items: MutableMap<String, FuelSurchargeAllocation>,
    var order: MutableList<String>,
) : FuelSurchargeAllocationStore {
    override fun find(code: String): FuelSurchargeAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeAllocation) {
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

    override fun list(): MutableList<FuelSurchargeAllocation> {
        val out = mutableListOf<FuelSurchargeAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeAllocationStore(): FuelSurchargeAllocationStore {
    return MemoryFuelSurchargeAllocationStore(
        mutableMapOf<String, FuelSurchargeAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeAllocationStore(
    var inner: FuelSurchargeAllocationStore,
    var cache: TtlCache<FuelSurchargeAllocation>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeAllocationStore {
    override fun find(code: String): FuelSurchargeAllocation? {
        val hit = (cache.get(code) as FuelSurchargeAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeAllocation> = inner.list()
}

fun newCachedFuelSurchargeAllocationStore(inner: FuelSurchargeAllocationStore, ttl: Int, clock: Clock): FuelSurchargeAllocationStore {
    return CachedFuelSurchargeAllocationStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge allocations. Empty fields match all. */
data class FuelSurchargeAllocationQuery(
    var locale: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeAllocation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeAllocation>): MutableList<FuelSurchargeAllocation> {
        val matched = filter(items) { candidate: FuelSurchargeAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeAllocations(store: FuelSurchargeAllocationStore, query: FuelSurchargeAllocationQuery): MutableList<FuelSurchargeAllocation> {
    return query.apply(store.list())
}
