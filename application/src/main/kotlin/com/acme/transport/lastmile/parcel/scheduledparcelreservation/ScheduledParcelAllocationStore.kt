package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled parcel allocations by code. */
interface ScheduledParcelAllocationStore {
    fun find(code: String): ScheduledParcelAllocation?
    fun save(item: ScheduledParcelAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledParcelAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledParcelAllocationStore(
    var items: MutableMap<String, ScheduledParcelAllocation>,
    var order: MutableList<String>,
) : ScheduledParcelAllocationStore {
    override fun find(code: String): ScheduledParcelAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledParcelAllocation) {
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

    override fun list(): MutableList<ScheduledParcelAllocation> {
        val out = mutableListOf<ScheduledParcelAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledParcelAllocationStore(): ScheduledParcelAllocationStore {
    return MemoryScheduledParcelAllocationStore(
        mutableMapOf<String, ScheduledParcelAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledParcelAllocationStore(
    var inner: ScheduledParcelAllocationStore,
    var cache: TtlCache<ScheduledParcelAllocation>,
    var hits: Int,
    var misses: Int,
) : ScheduledParcelAllocationStore {
    override fun find(code: String): ScheduledParcelAllocation? {
        val hit = (cache.get(code) as ScheduledParcelAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledParcelAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledParcelAllocation> = inner.list()
}

fun newCachedScheduledParcelAllocationStore(inner: ScheduledParcelAllocationStore, ttl: Int, clock: Clock): ScheduledParcelAllocationStore {
    return CachedScheduledParcelAllocationStore(
        inner = inner,
        cache = newTtlCache<ScheduledParcelAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled parcel allocations. Empty fields match all. */
data class ScheduledParcelAllocationQuery(
    var locale: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledParcelAllocation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledParcelAllocation>): MutableList<ScheduledParcelAllocation> {
        val matched = filter(items) { candidate: ScheduledParcelAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledParcelAllocations(store: ScheduledParcelAllocationStore, query: ScheduledParcelAllocationQuery): MutableList<ScheduledParcelAllocation> {
    return query.apply(store.list())
}
