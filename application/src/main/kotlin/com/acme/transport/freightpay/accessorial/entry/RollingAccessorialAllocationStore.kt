package com.acme.transport.freightpay.accessorial.entry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling accessorial allocations by code. */
interface RollingAccessorialAllocationStore {
    fun find(code: String): RollingAccessorialAllocation?
    fun save(item: RollingAccessorialAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAccessorialAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAccessorialAllocationStore(
    var items: MutableMap<String, RollingAccessorialAllocation>,
    var order: MutableList<String>,
) : RollingAccessorialAllocationStore {
    override fun find(code: String): RollingAccessorialAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAccessorialAllocation) {
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

    override fun list(): MutableList<RollingAccessorialAllocation> {
        val out = mutableListOf<RollingAccessorialAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAccessorialAllocationStore(): RollingAccessorialAllocationStore {
    return MemoryRollingAccessorialAllocationStore(
        mutableMapOf<String, RollingAccessorialAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAccessorialAllocationStore(
    var inner: RollingAccessorialAllocationStore,
    var cache: TtlCache<RollingAccessorialAllocation>,
    var hits: Int,
    var misses: Int,
) : RollingAccessorialAllocationStore {
    override fun find(code: String): RollingAccessorialAllocation? {
        val hit = (cache.get(code) as RollingAccessorialAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAccessorialAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAccessorialAllocation> = inner.list()
}

fun newCachedRollingAccessorialAllocationStore(inner: RollingAccessorialAllocationStore, ttl: Int, clock: Clock): RollingAccessorialAllocationStore {
    return CachedRollingAccessorialAllocationStore(
        inner = inner,
        cache = newTtlCache<RollingAccessorialAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling accessorial allocations. Empty fields match all. */
data class RollingAccessorialAllocationQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAccessorialAllocation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingAccessorialAllocation>): MutableList<RollingAccessorialAllocation> {
        val matched = filter(items) { candidate: RollingAccessorialAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAccessorialAllocations(store: RollingAccessorialAllocationStore, query: RollingAccessorialAllocationQuery): MutableList<RollingAccessorialAllocation> {
    return query.apply(store.list())
}
