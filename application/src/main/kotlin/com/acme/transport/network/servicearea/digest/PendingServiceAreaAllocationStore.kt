package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending service area allocations by code. */
interface PendingServiceAreaAllocationStore {
    fun find(code: String): PendingServiceAreaAllocation?
    fun save(item: PendingServiceAreaAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingServiceAreaAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingServiceAreaAllocationStore(
    var items: MutableMap<String, PendingServiceAreaAllocation>,
    var order: MutableList<String>,
) : PendingServiceAreaAllocationStore {
    override fun find(code: String): PendingServiceAreaAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingServiceAreaAllocation) {
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

    override fun list(): MutableList<PendingServiceAreaAllocation> {
        val out = mutableListOf<PendingServiceAreaAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingServiceAreaAllocationStore(): PendingServiceAreaAllocationStore {
    return MemoryPendingServiceAreaAllocationStore(
        mutableMapOf<String, PendingServiceAreaAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingServiceAreaAllocationStore(
    var inner: PendingServiceAreaAllocationStore,
    var cache: TtlCache<PendingServiceAreaAllocation>,
    var hits: Int,
    var misses: Int,
) : PendingServiceAreaAllocationStore {
    override fun find(code: String): PendingServiceAreaAllocation? {
        val hit = (cache.get(code) as PendingServiceAreaAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingServiceAreaAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingServiceAreaAllocation> = inner.list()
}

fun newCachedPendingServiceAreaAllocationStore(inner: PendingServiceAreaAllocationStore, ttl: Int, clock: Clock): PendingServiceAreaAllocationStore {
    return CachedPendingServiceAreaAllocationStore(
        inner = inner,
        cache = newTtlCache<PendingServiceAreaAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending service area allocations. Empty fields match all. */
data class PendingServiceAreaAllocationQuery(
    var depot: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingServiceAreaAllocation): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingServiceAreaAllocation>): MutableList<PendingServiceAreaAllocation> {
        val matched = filter(items) { candidate: PendingServiceAreaAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingServiceAreaAllocations(store: PendingServiceAreaAllocationStore, query: PendingServiceAreaAllocationQuery): MutableList<PendingServiceAreaAllocation> {
    return query.apply(store.list())
}
