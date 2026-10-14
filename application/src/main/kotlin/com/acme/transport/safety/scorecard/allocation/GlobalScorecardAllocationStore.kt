package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global scorecard allocations by code. */
interface GlobalScorecardAllocationStore {
    fun find(code: String): GlobalScorecardAllocation?
    fun save(item: GlobalScorecardAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalScorecardAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalScorecardAllocationStore(
    var items: MutableMap<String, GlobalScorecardAllocation>,
    var order: MutableList<String>,
) : GlobalScorecardAllocationStore {
    override fun find(code: String): GlobalScorecardAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalScorecardAllocation) {
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

    override fun list(): MutableList<GlobalScorecardAllocation> {
        val out = mutableListOf<GlobalScorecardAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalScorecardAllocationStore(): GlobalScorecardAllocationStore {
    return MemoryGlobalScorecardAllocationStore(
        mutableMapOf<String, GlobalScorecardAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalScorecardAllocationStore(
    var inner: GlobalScorecardAllocationStore,
    var cache: TtlCache<GlobalScorecardAllocation>,
    var hits: Int,
    var misses: Int,
) : GlobalScorecardAllocationStore {
    override fun find(code: String): GlobalScorecardAllocation? {
        val hit = (cache.get(code) as GlobalScorecardAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalScorecardAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalScorecardAllocation> = inner.list()
}

fun newCachedGlobalScorecardAllocationStore(inner: GlobalScorecardAllocationStore, ttl: Int, clock: Clock): GlobalScorecardAllocationStore {
    return CachedGlobalScorecardAllocationStore(
        inner = inner,
        cache = newTtlCache<GlobalScorecardAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global scorecard allocations. Empty fields match all. */
data class GlobalScorecardAllocationQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalScorecardAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalScorecardAllocation>): MutableList<GlobalScorecardAllocation> {
        val matched = filter(items) { candidate: GlobalScorecardAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalScorecardAllocations(store: GlobalScorecardAllocationStore, query: GlobalScorecardAllocationQuery): MutableList<GlobalScorecardAllocation> {
    return query.apply(store.list())
}
