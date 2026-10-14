package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary zone allocations by code. */
interface PrimaryZoneAllocationStore {
    fun find(code: String): PrimaryZoneAllocation?
    fun save(item: PrimaryZoneAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryZoneAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryZoneAllocationStore(
    var items: MutableMap<String, PrimaryZoneAllocation>,
    var order: MutableList<String>,
) : PrimaryZoneAllocationStore {
    override fun find(code: String): PrimaryZoneAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryZoneAllocation) {
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

    override fun list(): MutableList<PrimaryZoneAllocation> {
        val out = mutableListOf<PrimaryZoneAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryZoneAllocationStore(): PrimaryZoneAllocationStore {
    return MemoryPrimaryZoneAllocationStore(
        mutableMapOf<String, PrimaryZoneAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryZoneAllocationStore(
    var inner: PrimaryZoneAllocationStore,
    var cache: TtlCache<PrimaryZoneAllocation>,
    var hits: Int,
    var misses: Int,
) : PrimaryZoneAllocationStore {
    override fun find(code: String): PrimaryZoneAllocation? {
        val hit = (cache.get(code) as PrimaryZoneAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryZoneAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryZoneAllocation> = inner.list()
}

fun newCachedPrimaryZoneAllocationStore(inner: PrimaryZoneAllocationStore, ttl: Int, clock: Clock): PrimaryZoneAllocationStore {
    return CachedPrimaryZoneAllocationStore(
        inner = inner,
        cache = newTtlCache<PrimaryZoneAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary zone allocations. Empty fields match all. */
data class PrimaryZoneAllocationQuery(
    var source: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryZoneAllocation): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<PrimaryZoneAllocation>): MutableList<PrimaryZoneAllocation> {
        val matched = filter(items) { candidate: PrimaryZoneAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryZoneAllocations(store: PrimaryZoneAllocationStore, query: PrimaryZoneAllocationQuery): MutableList<PrimaryZoneAllocation> {
    return query.apply(store.list())
}
