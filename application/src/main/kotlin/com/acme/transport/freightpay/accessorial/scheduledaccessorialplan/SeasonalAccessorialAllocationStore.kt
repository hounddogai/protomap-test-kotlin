package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal accessorial allocations by code. */
interface SeasonalAccessorialAllocationStore {
    fun find(code: String): SeasonalAccessorialAllocation?
    fun save(item: SeasonalAccessorialAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAccessorialAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAccessorialAllocationStore(
    var items: MutableMap<String, SeasonalAccessorialAllocation>,
    var order: MutableList<String>,
) : SeasonalAccessorialAllocationStore {
    override fun find(code: String): SeasonalAccessorialAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAccessorialAllocation) {
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

    override fun list(): MutableList<SeasonalAccessorialAllocation> {
        val out = mutableListOf<SeasonalAccessorialAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAccessorialAllocationStore(): SeasonalAccessorialAllocationStore {
    return MemorySeasonalAccessorialAllocationStore(
        mutableMapOf<String, SeasonalAccessorialAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAccessorialAllocationStore(
    var inner: SeasonalAccessorialAllocationStore,
    var cache: TtlCache<SeasonalAccessorialAllocation>,
    var hits: Int,
    var misses: Int,
) : SeasonalAccessorialAllocationStore {
    override fun find(code: String): SeasonalAccessorialAllocation? {
        val hit = (cache.get(code) as SeasonalAccessorialAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAccessorialAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAccessorialAllocation> = inner.list()
}

fun newCachedSeasonalAccessorialAllocationStore(inner: SeasonalAccessorialAllocationStore, ttl: Int, clock: Clock): SeasonalAccessorialAllocationStore {
    return CachedSeasonalAccessorialAllocationStore(
        inner = inner,
        cache = newTtlCache<SeasonalAccessorialAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal accessorial allocations. Empty fields match all. */
data class SeasonalAccessorialAllocationQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAccessorialAllocation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<SeasonalAccessorialAllocation>): MutableList<SeasonalAccessorialAllocation> {
        val matched = filter(items) { candidate: SeasonalAccessorialAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAccessorialAllocations(store: SeasonalAccessorialAllocationStore, query: SeasonalAccessorialAllocationQuery): MutableList<SeasonalAccessorialAllocation> {
    return query.apply(store.list())
}
