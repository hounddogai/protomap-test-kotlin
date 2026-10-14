package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation allocations by code. */
interface ViolationAllocationStore {
    fun find(code: String): ViolationAllocation?
    fun save(item: ViolationAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationAllocationStore(
    var items: MutableMap<String, ViolationAllocation>,
    var order: MutableList<String>,
) : ViolationAllocationStore {
    override fun find(code: String): ViolationAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationAllocation) {
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

    override fun list(): MutableList<ViolationAllocation> {
        val out = mutableListOf<ViolationAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationAllocationStore(): ViolationAllocationStore {
    return MemoryViolationAllocationStore(mutableMapOf<String, ViolationAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationAllocationStore(
    var inner: ViolationAllocationStore,
    var cache: TtlCache<ViolationAllocation>,
    var hits: Int,
    var misses: Int,
) : ViolationAllocationStore {
    override fun find(code: String): ViolationAllocation? {
        val hit = (cache.get(code) as ViolationAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationAllocation> = inner.list()
}

fun newCachedViolationAllocationStore(inner: ViolationAllocationStore, ttl: Int, clock: Clock): ViolationAllocationStore {
    return CachedViolationAllocationStore(
        inner = inner,
        cache = newTtlCache<ViolationAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation allocations. Empty fields match all. */
data class ViolationAllocationQuery(
    var segment: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationAllocation): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationAllocation>): MutableList<ViolationAllocation> {
        val matched = filter(items) { candidate: ViolationAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationAllocations(store: ViolationAllocationStore, query: ViolationAllocationQuery): MutableList<ViolationAllocation> {
    return query.apply(store.list())
}
