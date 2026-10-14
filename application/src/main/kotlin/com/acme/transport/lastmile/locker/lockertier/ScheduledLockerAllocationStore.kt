package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled locker allocations by code. */
interface ScheduledLockerAllocationStore {
    fun find(code: String): ScheduledLockerAllocation?
    fun save(item: ScheduledLockerAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLockerAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLockerAllocationStore(
    var items: MutableMap<String, ScheduledLockerAllocation>,
    var order: MutableList<String>,
) : ScheduledLockerAllocationStore {
    override fun find(code: String): ScheduledLockerAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLockerAllocation) {
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

    override fun list(): MutableList<ScheduledLockerAllocation> {
        val out = mutableListOf<ScheduledLockerAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLockerAllocationStore(): ScheduledLockerAllocationStore {
    return MemoryScheduledLockerAllocationStore(
        mutableMapOf<String, ScheduledLockerAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLockerAllocationStore(
    var inner: ScheduledLockerAllocationStore,
    var cache: TtlCache<ScheduledLockerAllocation>,
    var hits: Int,
    var misses: Int,
) : ScheduledLockerAllocationStore {
    override fun find(code: String): ScheduledLockerAllocation? {
        val hit = (cache.get(code) as ScheduledLockerAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLockerAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLockerAllocation> = inner.list()
}

fun newCachedScheduledLockerAllocationStore(inner: ScheduledLockerAllocationStore, ttl: Int, clock: Clock): ScheduledLockerAllocationStore {
    return CachedScheduledLockerAllocationStore(
        inner = inner,
        cache = newTtlCache<ScheduledLockerAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled locker allocations. Empty fields match all. */
data class ScheduledLockerAllocationQuery(
    var unit: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLockerAllocation): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ScheduledLockerAllocation>): MutableList<ScheduledLockerAllocation> {
        val matched = filter(items) { candidate: ScheduledLockerAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLockerAllocations(store: ScheduledLockerAllocationStore, query: ScheduledLockerAllocationQuery): MutableList<ScheduledLockerAllocation> {
    return query.apply(store.list())
}
