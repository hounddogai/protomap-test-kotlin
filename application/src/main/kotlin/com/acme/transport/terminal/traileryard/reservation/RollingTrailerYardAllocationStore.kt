package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling trailer yard allocations by code. */
interface RollingTrailerYardAllocationStore {
    fun find(code: String): RollingTrailerYardAllocation?
    fun save(item: RollingTrailerYardAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTrailerYardAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTrailerYardAllocationStore(
    var items: MutableMap<String, RollingTrailerYardAllocation>,
    var order: MutableList<String>,
) : RollingTrailerYardAllocationStore {
    override fun find(code: String): RollingTrailerYardAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTrailerYardAllocation) {
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

    override fun list(): MutableList<RollingTrailerYardAllocation> {
        val out = mutableListOf<RollingTrailerYardAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTrailerYardAllocationStore(): RollingTrailerYardAllocationStore {
    return MemoryRollingTrailerYardAllocationStore(
        mutableMapOf<String, RollingTrailerYardAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTrailerYardAllocationStore(
    var inner: RollingTrailerYardAllocationStore,
    var cache: TtlCache<RollingTrailerYardAllocation>,
    var hits: Int,
    var misses: Int,
) : RollingTrailerYardAllocationStore {
    override fun find(code: String): RollingTrailerYardAllocation? {
        val hit = (cache.get(code) as RollingTrailerYardAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTrailerYardAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTrailerYardAllocation> = inner.list()
}

fun newCachedRollingTrailerYardAllocationStore(inner: RollingTrailerYardAllocationStore, ttl: Int, clock: Clock): RollingTrailerYardAllocationStore {
    return CachedRollingTrailerYardAllocationStore(
        inner = inner,
        cache = newTtlCache<RollingTrailerYardAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling trailer yard allocations. Empty fields match all. */
data class RollingTrailerYardAllocationQuery(
    var lane: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTrailerYardAllocation): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<RollingTrailerYardAllocation>): MutableList<RollingTrailerYardAllocation> {
        val matched = filter(items) { candidate: RollingTrailerYardAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTrailerYardAllocations(store: RollingTrailerYardAllocationStore, query: RollingTrailerYardAllocationQuery): MutableList<RollingTrailerYardAllocation> {
    return query.apply(store.list())
}
