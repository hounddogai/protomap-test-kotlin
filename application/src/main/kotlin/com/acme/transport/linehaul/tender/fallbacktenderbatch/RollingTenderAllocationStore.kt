package com.acme.transport.linehaul.tender.fallbacktenderbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling tender allocations by code. */
interface RollingTenderAllocationStore {
    fun find(code: String): RollingTenderAllocation?
    fun save(item: RollingTenderAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTenderAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTenderAllocationStore(
    var items: MutableMap<String, RollingTenderAllocation>,
    var order: MutableList<String>,
) : RollingTenderAllocationStore {
    override fun find(code: String): RollingTenderAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTenderAllocation) {
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

    override fun list(): MutableList<RollingTenderAllocation> {
        val out = mutableListOf<RollingTenderAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTenderAllocationStore(): RollingTenderAllocationStore {
    return MemoryRollingTenderAllocationStore(
        mutableMapOf<String, RollingTenderAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTenderAllocationStore(
    var inner: RollingTenderAllocationStore,
    var cache: TtlCache<RollingTenderAllocation>,
    var hits: Int,
    var misses: Int,
) : RollingTenderAllocationStore {
    override fun find(code: String): RollingTenderAllocation? {
        val hit = (cache.get(code) as RollingTenderAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTenderAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTenderAllocation> = inner.list()
}

fun newCachedRollingTenderAllocationStore(inner: RollingTenderAllocationStore, ttl: Int, clock: Clock): RollingTenderAllocationStore {
    return CachedRollingTenderAllocationStore(
        inner = inner,
        cache = newTtlCache<RollingTenderAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling tender allocations. Empty fields match all. */
data class RollingTenderAllocationQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTenderAllocation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTenderAllocation>): MutableList<RollingTenderAllocation> {
        val matched = filter(items) { candidate: RollingTenderAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTenderAllocations(store: RollingTenderAllocationStore, query: RollingTenderAllocationQuery): MutableList<RollingTenderAllocation> {
    return query.apply(store.list())
}
