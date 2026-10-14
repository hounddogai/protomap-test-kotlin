package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted accessorial allocations by code. */
interface WeightedAccessorialAllocationStore {
    fun find(code: String): WeightedAccessorialAllocation?
    fun save(item: WeightedAccessorialAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAccessorialAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAccessorialAllocationStore(
    var items: MutableMap<String, WeightedAccessorialAllocation>,
    var order: MutableList<String>,
) : WeightedAccessorialAllocationStore {
    override fun find(code: String): WeightedAccessorialAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAccessorialAllocation) {
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

    override fun list(): MutableList<WeightedAccessorialAllocation> {
        val out = mutableListOf<WeightedAccessorialAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAccessorialAllocationStore(): WeightedAccessorialAllocationStore {
    return MemoryWeightedAccessorialAllocationStore(
        mutableMapOf<String, WeightedAccessorialAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAccessorialAllocationStore(
    var inner: WeightedAccessorialAllocationStore,
    var cache: TtlCache<WeightedAccessorialAllocation>,
    var hits: Int,
    var misses: Int,
) : WeightedAccessorialAllocationStore {
    override fun find(code: String): WeightedAccessorialAllocation? {
        val hit = (cache.get(code) as WeightedAccessorialAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAccessorialAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAccessorialAllocation> = inner.list()
}

fun newCachedWeightedAccessorialAllocationStore(inner: WeightedAccessorialAllocationStore, ttl: Int, clock: Clock): WeightedAccessorialAllocationStore {
    return CachedWeightedAccessorialAllocationStore(
        inner = inner,
        cache = newTtlCache<WeightedAccessorialAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted accessorial allocations. Empty fields match all. */
data class WeightedAccessorialAllocationQuery(
    var locale: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAccessorialAllocation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAccessorialAllocation>): MutableList<WeightedAccessorialAllocation> {
        val matched = filter(items) { candidate: WeightedAccessorialAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAccessorialAllocations(store: WeightedAccessorialAllocationStore, query: WeightedAccessorialAllocationQuery): MutableList<WeightedAccessorialAllocation> {
    return query.apply(store.list())
}
