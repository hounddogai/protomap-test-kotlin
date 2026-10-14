package com.acme.transport.fleetops.defect.allocation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect allocations by code. */
interface DefectAllocationStore {
    fun find(code: String): DefectAllocation?
    fun save(item: DefectAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectAllocationStore(
    var items: MutableMap<String, DefectAllocation>,
    var order: MutableList<String>,
) : DefectAllocationStore {
    override fun find(code: String): DefectAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectAllocation) {
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

    override fun list(): MutableList<DefectAllocation> {
        val out = mutableListOf<DefectAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectAllocationStore(): DefectAllocationStore {
    return MemoryDefectAllocationStore(mutableMapOf<String, DefectAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectAllocationStore(
    var inner: DefectAllocationStore,
    var cache: TtlCache<DefectAllocation>,
    var hits: Int,
    var misses: Int,
) : DefectAllocationStore {
    override fun find(code: String): DefectAllocation? {
        val hit = (cache.get(code) as DefectAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectAllocation> = inner.list()
}

fun newCachedDefectAllocationStore(inner: DefectAllocationStore, ttl: Int, clock: Clock): DefectAllocationStore {
    return CachedDefectAllocationStore(
        inner = inner,
        cache = newTtlCache<DefectAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect allocations. Empty fields match all. */
data class DefectAllocationQuery(
    var channel: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectAllocation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectAllocation>): MutableList<DefectAllocation> {
        val matched = filter(items) { candidate: DefectAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectAllocations(store: DefectAllocationStore, query: DefectAllocationQuery): MutableList<DefectAllocation> {
    return query.apply(store.list())
}
