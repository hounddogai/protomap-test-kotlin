package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled dwell allocations by code. */
interface ScheduledDwellAllocationStore {
    fun find(code: String): ScheduledDwellAllocation?
    fun save(item: ScheduledDwellAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDwellAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDwellAllocationStore(
    var items: MutableMap<String, ScheduledDwellAllocation>,
    var order: MutableList<String>,
) : ScheduledDwellAllocationStore {
    override fun find(code: String): ScheduledDwellAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDwellAllocation) {
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

    override fun list(): MutableList<ScheduledDwellAllocation> {
        val out = mutableListOf<ScheduledDwellAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDwellAllocationStore(): ScheduledDwellAllocationStore {
    return MemoryScheduledDwellAllocationStore(
        mutableMapOf<String, ScheduledDwellAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDwellAllocationStore(
    var inner: ScheduledDwellAllocationStore,
    var cache: TtlCache<ScheduledDwellAllocation>,
    var hits: Int,
    var misses: Int,
) : ScheduledDwellAllocationStore {
    override fun find(code: String): ScheduledDwellAllocation? {
        val hit = (cache.get(code) as ScheduledDwellAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDwellAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDwellAllocation> = inner.list()
}

fun newCachedScheduledDwellAllocationStore(inner: ScheduledDwellAllocationStore, ttl: Int, clock: Clock): ScheduledDwellAllocationStore {
    return CachedScheduledDwellAllocationStore(
        inner = inner,
        cache = newTtlCache<ScheduledDwellAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled dwell allocations. Empty fields match all. */
data class ScheduledDwellAllocationQuery(
    var category: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDwellAllocation): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDwellAllocation>): MutableList<ScheduledDwellAllocation> {
        val matched = filter(items) { candidate: ScheduledDwellAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDwellAllocations(store: ScheduledDwellAllocationStore, query: ScheduledDwellAllocationQuery): MutableList<ScheduledDwellAllocation> {
    return query.apply(store.list())
}
