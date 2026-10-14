package com.acme.transport.safety.violation.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback violation allocations by code. */
interface FallbackViolationAllocationStore {
    fun find(code: String): FallbackViolationAllocation?
    fun save(item: FallbackViolationAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackViolationAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackViolationAllocationStore(
    var items: MutableMap<String, FallbackViolationAllocation>,
    var order: MutableList<String>,
) : FallbackViolationAllocationStore {
    override fun find(code: String): FallbackViolationAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackViolationAllocation) {
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

    override fun list(): MutableList<FallbackViolationAllocation> {
        val out = mutableListOf<FallbackViolationAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackViolationAllocationStore(): FallbackViolationAllocationStore {
    return MemoryFallbackViolationAllocationStore(
        mutableMapOf<String, FallbackViolationAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackViolationAllocationStore(
    var inner: FallbackViolationAllocationStore,
    var cache: TtlCache<FallbackViolationAllocation>,
    var hits: Int,
    var misses: Int,
) : FallbackViolationAllocationStore {
    override fun find(code: String): FallbackViolationAllocation? {
        val hit = (cache.get(code) as FallbackViolationAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackViolationAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackViolationAllocation> = inner.list()
}

fun newCachedFallbackViolationAllocationStore(inner: FallbackViolationAllocationStore, ttl: Int, clock: Clock): FallbackViolationAllocationStore {
    return CachedFallbackViolationAllocationStore(
        inner = inner,
        cache = newTtlCache<FallbackViolationAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback violation allocations. Empty fields match all. */
data class FallbackViolationAllocationQuery(
    var lane: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackViolationAllocation): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackViolationAllocation>): MutableList<FallbackViolationAllocation> {
        val matched = filter(items) { candidate: FallbackViolationAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackViolationAllocations(store: FallbackViolationAllocationStore, query: FallbackViolationAllocationQuery): MutableList<FallbackViolationAllocation> {
    return query.apply(store.list())
}
