package com.acme.transport.freightpay.ratecard.cardsnapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card allocations by code. */
interface RateCardAllocationStore {
    fun find(code: String): RateCardAllocation?
    fun save(item: RateCardAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardAllocationStore(
    var items: MutableMap<String, RateCardAllocation>,
    var order: MutableList<String>,
) : RateCardAllocationStore {
    override fun find(code: String): RateCardAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardAllocation) {
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

    override fun list(): MutableList<RateCardAllocation> {
        val out = mutableListOf<RateCardAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardAllocationStore(): RateCardAllocationStore {
    return MemoryRateCardAllocationStore(mutableMapOf<String, RateCardAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardAllocationStore(
    var inner: RateCardAllocationStore,
    var cache: TtlCache<RateCardAllocation>,
    var hits: Int,
    var misses: Int,
) : RateCardAllocationStore {
    override fun find(code: String): RateCardAllocation? {
        val hit = (cache.get(code) as RateCardAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardAllocation> = inner.list()
}

fun newCachedRateCardAllocationStore(inner: RateCardAllocationStore, ttl: Int, clock: Clock): RateCardAllocationStore {
    return CachedRateCardAllocationStore(
        inner = inner,
        cache = newTtlCache<RateCardAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card allocations. Empty fields match all. */
data class RateCardAllocationQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardAllocation>): MutableList<RateCardAllocation> {
        val matched = filter(items) { candidate: RateCardAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardAllocations(store: RateCardAllocationStore, query: RateCardAllocationQuery): MutableList<RateCardAllocation> {
    return query.apply(store.list())
}
