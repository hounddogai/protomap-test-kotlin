package com.acme.transport.crewing.roster.mapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster allocations by code. */
interface RosterAllocationStore {
    fun find(code: String): RosterAllocation?
    fun save(item: RosterAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterAllocationStore(
    var items: MutableMap<String, RosterAllocation>,
    var order: MutableList<String>,
) : RosterAllocationStore {
    override fun find(code: String): RosterAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterAllocation) {
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

    override fun list(): MutableList<RosterAllocation> {
        val out = mutableListOf<RosterAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterAllocationStore(): RosterAllocationStore {
    return MemoryRosterAllocationStore(mutableMapOf<String, RosterAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterAllocationStore(
    var inner: RosterAllocationStore,
    var cache: TtlCache<RosterAllocation>,
    var hits: Int,
    var misses: Int,
) : RosterAllocationStore {
    override fun find(code: String): RosterAllocation? {
        val hit = (cache.get(code) as RosterAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterAllocation> = inner.list()
}

fun newCachedRosterAllocationStore(inner: RosterAllocationStore, ttl: Int, clock: Clock): RosterAllocationStore {
    return CachedRosterAllocationStore(
        inner = inner,
        cache = newTtlCache<RosterAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster allocations. Empty fields match all. */
data class RosterAllocationQuery(
    var category: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterAllocation): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterAllocation>): MutableList<RosterAllocation> {
        val matched = filter(items) { candidate: RosterAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterAllocations(store: RosterAllocationStore, query: RosterAllocationQuery): MutableList<RosterAllocation> {
    return query.apply(store.list())
}
