package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient accessorial allocations by code. */
interface LenientAccessorialAllocationStore {
    fun find(code: String): LenientAccessorialAllocation?
    fun save(item: LenientAccessorialAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAccessorialAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAccessorialAllocationStore(
    var items: MutableMap<String, LenientAccessorialAllocation>,
    var order: MutableList<String>,
) : LenientAccessorialAllocationStore {
    override fun find(code: String): LenientAccessorialAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAccessorialAllocation) {
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

    override fun list(): MutableList<LenientAccessorialAllocation> {
        val out = mutableListOf<LenientAccessorialAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAccessorialAllocationStore(): LenientAccessorialAllocationStore {
    return MemoryLenientAccessorialAllocationStore(
        mutableMapOf<String, LenientAccessorialAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAccessorialAllocationStore(
    var inner: LenientAccessorialAllocationStore,
    var cache: TtlCache<LenientAccessorialAllocation>,
    var hits: Int,
    var misses: Int,
) : LenientAccessorialAllocationStore {
    override fun find(code: String): LenientAccessorialAllocation? {
        val hit = (cache.get(code) as LenientAccessorialAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAccessorialAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAccessorialAllocation> = inner.list()
}

fun newCachedLenientAccessorialAllocationStore(inner: LenientAccessorialAllocationStore, ttl: Int, clock: Clock): LenientAccessorialAllocationStore {
    return CachedLenientAccessorialAllocationStore(
        inner = inner,
        cache = newTtlCache<LenientAccessorialAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient accessorial allocations. Empty fields match all. */
data class LenientAccessorialAllocationQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAccessorialAllocation): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<LenientAccessorialAllocation>): MutableList<LenientAccessorialAllocation> {
        val matched = filter(items) { candidate: LenientAccessorialAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAccessorialAllocations(store: LenientAccessorialAllocationStore, query: LenientAccessorialAllocationQuery): MutableList<LenientAccessorialAllocation> {
    return query.apply(store.list())
}
