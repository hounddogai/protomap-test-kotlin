package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled route offset allocations by code. */
interface ScheduledRouteOffsetAllocationStore {
    fun find(code: String): ScheduledRouteOffsetAllocation?
    fun save(item: ScheduledRouteOffsetAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRouteOffsetAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRouteOffsetAllocationStore(
    var items: MutableMap<String, ScheduledRouteOffsetAllocation>,
    var order: MutableList<String>,
) : ScheduledRouteOffsetAllocationStore {
    override fun find(code: String): ScheduledRouteOffsetAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRouteOffsetAllocation) {
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

    override fun list(): MutableList<ScheduledRouteOffsetAllocation> {
        val out = mutableListOf<ScheduledRouteOffsetAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRouteOffsetAllocationStore(): ScheduledRouteOffsetAllocationStore {
    return MemoryScheduledRouteOffsetAllocationStore(
        mutableMapOf<String, ScheduledRouteOffsetAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRouteOffsetAllocationStore(
    var inner: ScheduledRouteOffsetAllocationStore,
    var cache: TtlCache<ScheduledRouteOffsetAllocation>,
    var hits: Int,
    var misses: Int,
) : ScheduledRouteOffsetAllocationStore {
    override fun find(code: String): ScheduledRouteOffsetAllocation? {
        val hit = (cache.get(code) as ScheduledRouteOffsetAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRouteOffsetAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRouteOffsetAllocation> = inner.list()
}

fun newCachedScheduledRouteOffsetAllocationStore(inner: ScheduledRouteOffsetAllocationStore, ttl: Int, clock: Clock): ScheduledRouteOffsetAllocationStore {
    return CachedScheduledRouteOffsetAllocationStore(
        inner = inner,
        cache = newTtlCache<ScheduledRouteOffsetAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled route offset allocations. Empty fields match all. */
data class ScheduledRouteOffsetAllocationQuery(
    var channel: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRouteOffsetAllocation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRouteOffsetAllocation>): MutableList<ScheduledRouteOffsetAllocation> {
        val matched = filter(items) { candidate: ScheduledRouteOffsetAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRouteOffsetAllocations(store: ScheduledRouteOffsetAllocationStore, query: ScheduledRouteOffsetAllocationQuery): MutableList<ScheduledRouteOffsetAllocation> {
    return query.apply(store.list())
}
