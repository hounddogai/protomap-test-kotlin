package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps manual zone allocations by code. */
interface ManualZoneAllocationStore {
    fun find(code: String): ManualZoneAllocation?
    fun save(item: ManualZoneAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualZoneAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualZoneAllocationStore(
    var items: MutableMap<String, ManualZoneAllocation>,
    var order: MutableList<String>,
) : ManualZoneAllocationStore {
    override fun find(code: String): ManualZoneAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualZoneAllocation) {
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

    override fun list(): MutableList<ManualZoneAllocation> {
        val out = mutableListOf<ManualZoneAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualZoneAllocationStore(): ManualZoneAllocationStore {
    return MemoryManualZoneAllocationStore(
        mutableMapOf<String, ManualZoneAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualZoneAllocationStore(
    var inner: ManualZoneAllocationStore,
    var cache: TtlCache<ManualZoneAllocation>,
    var hits: Int,
    var misses: Int,
) : ManualZoneAllocationStore {
    override fun find(code: String): ManualZoneAllocation? {
        val hit = (cache.get(code) as ManualZoneAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualZoneAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualZoneAllocation> = inner.list()
}

fun newCachedManualZoneAllocationStore(inner: ManualZoneAllocationStore, ttl: Int, clock: Clock): ManualZoneAllocationStore {
    return CachedManualZoneAllocationStore(
        inner = inner,
        cache = newTtlCache<ManualZoneAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual zone allocations. Empty fields match all. */
data class ManualZoneAllocationQuery(
    var segment: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualZoneAllocation): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualZoneAllocation>): MutableList<ManualZoneAllocation> {
        val matched = filter(items) { candidate: ManualZoneAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualZoneAllocations(store: ManualZoneAllocationStore, query: ManualZoneAllocationQuery): MutableList<ManualZoneAllocation> {
    return query.apply(store.list())
}
