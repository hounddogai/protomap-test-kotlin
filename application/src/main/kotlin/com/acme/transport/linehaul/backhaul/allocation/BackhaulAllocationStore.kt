package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul allocations by code. */
interface BackhaulAllocationStore {
    fun find(code: String): BackhaulAllocation?
    fun save(item: BackhaulAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulAllocationStore(
    var items: MutableMap<String, BackhaulAllocation>,
    var order: MutableList<String>,
) : BackhaulAllocationStore {
    override fun find(code: String): BackhaulAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulAllocation) {
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

    override fun list(): MutableList<BackhaulAllocation> {
        val out = mutableListOf<BackhaulAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulAllocationStore(): BackhaulAllocationStore {
    return MemoryBackhaulAllocationStore(mutableMapOf<String, BackhaulAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulAllocationStore(
    var inner: BackhaulAllocationStore,
    var cache: TtlCache<BackhaulAllocation>,
    var hits: Int,
    var misses: Int,
) : BackhaulAllocationStore {
    override fun find(code: String): BackhaulAllocation? {
        val hit = (cache.get(code) as BackhaulAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulAllocation> = inner.list()
}

fun newCachedBackhaulAllocationStore(inner: BackhaulAllocationStore, ttl: Int, clock: Clock): BackhaulAllocationStore {
    return CachedBackhaulAllocationStore(
        inner = inner,
        cache = newTtlCache<BackhaulAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul allocations. Empty fields match all. */
data class BackhaulAllocationQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulAllocation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulAllocation>): MutableList<BackhaulAllocation> {
        val matched = filter(items) { candidate: BackhaulAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulAllocations(store: BackhaulAllocationStore, query: BackhaulAllocationQuery): MutableList<BackhaulAllocation> {
    return query.apply(store.list())
}
