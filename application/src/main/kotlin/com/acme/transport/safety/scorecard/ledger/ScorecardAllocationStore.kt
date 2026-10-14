package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard allocations by code. */
interface ScorecardAllocationStore {
    fun find(code: String): ScorecardAllocation?
    fun save(item: ScorecardAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardAllocationStore(
    var items: MutableMap<String, ScorecardAllocation>,
    var order: MutableList<String>,
) : ScorecardAllocationStore {
    override fun find(code: String): ScorecardAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardAllocation) {
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

    override fun list(): MutableList<ScorecardAllocation> {
        val out = mutableListOf<ScorecardAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardAllocationStore(): ScorecardAllocationStore {
    return MemoryScorecardAllocationStore(mutableMapOf<String, ScorecardAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardAllocationStore(
    var inner: ScorecardAllocationStore,
    var cache: TtlCache<ScorecardAllocation>,
    var hits: Int,
    var misses: Int,
) : ScorecardAllocationStore {
    override fun find(code: String): ScorecardAllocation? {
        val hit = (cache.get(code) as ScorecardAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardAllocation> = inner.list()
}

fun newCachedScorecardAllocationStore(inner: ScorecardAllocationStore, ttl: Int, clock: Clock): ScorecardAllocationStore {
    return CachedScorecardAllocationStore(
        inner = inner,
        cache = newTtlCache<ScorecardAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard allocations. Empty fields match all. */
data class ScorecardAllocationQuery(
    var country: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardAllocation): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardAllocation>): MutableList<ScorecardAllocation> {
        val matched = filter(items) { candidate: ScorecardAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardAllocations(store: ScorecardAllocationStore, query: ScorecardAllocationQuery): MutableList<ScorecardAllocation> {
    return query.apply(store.list())
}
