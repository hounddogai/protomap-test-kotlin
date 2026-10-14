package com.acme.transport.network.servicearea.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area allocations by code. */
interface ServiceAreaAllocationStore {
    fun find(code: String): ServiceAreaAllocation?
    fun save(item: ServiceAreaAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaAllocationStore(
    var items: MutableMap<String, ServiceAreaAllocation>,
    var order: MutableList<String>,
) : ServiceAreaAllocationStore {
    override fun find(code: String): ServiceAreaAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaAllocation) {
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

    override fun list(): MutableList<ServiceAreaAllocation> {
        val out = mutableListOf<ServiceAreaAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaAllocationStore(): ServiceAreaAllocationStore {
    return MemoryServiceAreaAllocationStore(
        mutableMapOf<String, ServiceAreaAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaAllocationStore(
    var inner: ServiceAreaAllocationStore,
    var cache: TtlCache<ServiceAreaAllocation>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaAllocationStore {
    override fun find(code: String): ServiceAreaAllocation? {
        val hit = (cache.get(code) as ServiceAreaAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaAllocation> = inner.list()
}

fun newCachedServiceAreaAllocationStore(inner: ServiceAreaAllocationStore, ttl: Int, clock: Clock): ServiceAreaAllocationStore {
    return CachedServiceAreaAllocationStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area allocations. Empty fields match all. */
data class ServiceAreaAllocationQuery(
    var lane: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaAllocation): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaAllocation>): MutableList<ServiceAreaAllocation> {
        val matched = filter(items) { candidate: ServiceAreaAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaAllocations(store: ServiceAreaAllocationStore, query: ServiceAreaAllocationQuery): MutableList<ServiceAreaAllocation> {
    return query.apply(store.list())
}
