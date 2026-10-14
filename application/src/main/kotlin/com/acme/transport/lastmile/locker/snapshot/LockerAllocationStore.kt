package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker allocations by code. */
interface LockerAllocationStore {
    fun find(code: String): LockerAllocation?
    fun save(item: LockerAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerAllocationStore(
    var items: MutableMap<String, LockerAllocation>,
    var order: MutableList<String>,
) : LockerAllocationStore {
    override fun find(code: String): LockerAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerAllocation) {
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

    override fun list(): MutableList<LockerAllocation> {
        val out = mutableListOf<LockerAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerAllocationStore(): LockerAllocationStore {
    return MemoryLockerAllocationStore(mutableMapOf<String, LockerAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerAllocationStore(
    var inner: LockerAllocationStore,
    var cache: TtlCache<LockerAllocation>,
    var hits: Int,
    var misses: Int,
) : LockerAllocationStore {
    override fun find(code: String): LockerAllocation? {
        val hit = (cache.get(code) as LockerAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerAllocation> = inner.list()
}

fun newCachedLockerAllocationStore(inner: LockerAllocationStore, ttl: Int, clock: Clock): LockerAllocationStore {
    return CachedLockerAllocationStore(
        inner = inner,
        cache = newTtlCache<LockerAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker allocations. Empty fields match all. */
data class LockerAllocationQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LockerAllocation>): MutableList<LockerAllocation> {
        val matched = filter(items) { candidate: LockerAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerAllocations(store: LockerAllocationStore, query: LockerAllocationQuery): MutableList<LockerAllocation> {
    return query.apply(store.list())
}
