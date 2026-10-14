package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default scorecard allocations by code. */
interface DefaultScorecardAllocationStore {
    fun find(code: String): DefaultScorecardAllocation?
    fun save(item: DefaultScorecardAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultScorecardAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultScorecardAllocationStore(
    var items: MutableMap<String, DefaultScorecardAllocation>,
    var order: MutableList<String>,
) : DefaultScorecardAllocationStore {
    override fun find(code: String): DefaultScorecardAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultScorecardAllocation) {
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

    override fun list(): MutableList<DefaultScorecardAllocation> {
        val out = mutableListOf<DefaultScorecardAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultScorecardAllocationStore(): DefaultScorecardAllocationStore {
    return MemoryDefaultScorecardAllocationStore(
        mutableMapOf<String, DefaultScorecardAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultScorecardAllocationStore(
    var inner: DefaultScorecardAllocationStore,
    var cache: TtlCache<DefaultScorecardAllocation>,
    var hits: Int,
    var misses: Int,
) : DefaultScorecardAllocationStore {
    override fun find(code: String): DefaultScorecardAllocation? {
        val hit = (cache.get(code) as DefaultScorecardAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultScorecardAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultScorecardAllocation> = inner.list()
}

fun newCachedDefaultScorecardAllocationStore(inner: DefaultScorecardAllocationStore, ttl: Int, clock: Clock): DefaultScorecardAllocationStore {
    return CachedDefaultScorecardAllocationStore(
        inner = inner,
        cache = newTtlCache<DefaultScorecardAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default scorecard allocations. Empty fields match all. */
data class DefaultScorecardAllocationQuery(
    var depot: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultScorecardAllocation): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<DefaultScorecardAllocation>): MutableList<DefaultScorecardAllocation> {
        val matched = filter(items) { candidate: DefaultScorecardAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultScorecardAllocations(store: DefaultScorecardAllocationStore, query: DefaultScorecardAllocationQuery): MutableList<DefaultScorecardAllocation> {
    return query.apply(store.list())
}
