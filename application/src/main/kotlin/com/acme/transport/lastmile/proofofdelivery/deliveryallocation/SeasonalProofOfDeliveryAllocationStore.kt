package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal proof of delivery allocations by code. */
interface SeasonalProofOfDeliveryAllocationStore {
    fun find(code: String): SeasonalProofOfDeliveryAllocation?
    fun save(item: SeasonalProofOfDeliveryAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalProofOfDeliveryAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalProofOfDeliveryAllocationStore(
    var items: MutableMap<String, SeasonalProofOfDeliveryAllocation>,
    var order: MutableList<String>,
) : SeasonalProofOfDeliveryAllocationStore {
    override fun find(code: String): SeasonalProofOfDeliveryAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalProofOfDeliveryAllocation) {
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

    override fun list(): MutableList<SeasonalProofOfDeliveryAllocation> {
        val out = mutableListOf<SeasonalProofOfDeliveryAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalProofOfDeliveryAllocationStore(): SeasonalProofOfDeliveryAllocationStore {
    return MemorySeasonalProofOfDeliveryAllocationStore(
        mutableMapOf<String, SeasonalProofOfDeliveryAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalProofOfDeliveryAllocationStore(
    var inner: SeasonalProofOfDeliveryAllocationStore,
    var cache: TtlCache<SeasonalProofOfDeliveryAllocation>,
    var hits: Int,
    var misses: Int,
) : SeasonalProofOfDeliveryAllocationStore {
    override fun find(code: String): SeasonalProofOfDeliveryAllocation? {
        val hit = (cache.get(code) as SeasonalProofOfDeliveryAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalProofOfDeliveryAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalProofOfDeliveryAllocation> = inner.list()
}

fun newCachedSeasonalProofOfDeliveryAllocationStore(inner: SeasonalProofOfDeliveryAllocationStore, ttl: Int, clock: Clock): SeasonalProofOfDeliveryAllocationStore {
    return CachedSeasonalProofOfDeliveryAllocationStore(
        inner = inner,
        cache = newTtlCache<SeasonalProofOfDeliveryAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal proof of delivery allocations. Empty fields match all. */
data class SeasonalProofOfDeliveryAllocationQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalProofOfDeliveryAllocation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalProofOfDeliveryAllocation>): MutableList<SeasonalProofOfDeliveryAllocation> {
        val matched = filter(items) { candidate: SeasonalProofOfDeliveryAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalProofOfDeliveryAllocations(store: SeasonalProofOfDeliveryAllocationStore, query: SeasonalProofOfDeliveryAllocationQuery): MutableList<SeasonalProofOfDeliveryAllocation> {
    return query.apply(store.list())
}
