package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard allocations by code. */
interface TrailerYardAllocationStore {
    fun find(code: String): TrailerYardAllocation?
    fun save(item: TrailerYardAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardAllocationStore(
    var items: MutableMap<String, TrailerYardAllocation>,
    var order: MutableList<String>,
) : TrailerYardAllocationStore {
    override fun find(code: String): TrailerYardAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardAllocation) {
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

    override fun list(): MutableList<TrailerYardAllocation> {
        val out = mutableListOf<TrailerYardAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardAllocationStore(): TrailerYardAllocationStore {
    return MemoryTrailerYardAllocationStore(
        mutableMapOf<String, TrailerYardAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardAllocationStore(
    var inner: TrailerYardAllocationStore,
    var cache: TtlCache<TrailerYardAllocation>,
    var hits: Int,
    var misses: Int,
) : TrailerYardAllocationStore {
    override fun find(code: String): TrailerYardAllocation? {
        val hit = (cache.get(code) as TrailerYardAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardAllocation> = inner.list()
}

fun newCachedTrailerYardAllocationStore(inner: TrailerYardAllocationStore, ttl: Int, clock: Clock): TrailerYardAllocationStore {
    return CachedTrailerYardAllocationStore(
        inner = inner,
        cache = newTtlCache<TrailerYardAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard allocations. Empty fields match all. */
data class TrailerYardAllocationQuery(
    var segment: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardAllocation): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardAllocation>): MutableList<TrailerYardAllocation> {
        val matched = filter(items) { candidate: TrailerYardAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardAllocations(store: TrailerYardAllocationStore, query: TrailerYardAllocationQuery): MutableList<TrailerYardAllocation> {
    return query.apply(store.list())
}
