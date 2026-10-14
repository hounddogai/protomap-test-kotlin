package com.acme.transport.terminal.crossdock.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped cross dock allocations by code. */
interface CappedCrossDockAllocationStore {
    fun find(code: String): CappedCrossDockAllocation?
    fun save(item: CappedCrossDockAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCrossDockAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCrossDockAllocationStore(
    var items: MutableMap<String, CappedCrossDockAllocation>,
    var order: MutableList<String>,
) : CappedCrossDockAllocationStore {
    override fun find(code: String): CappedCrossDockAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCrossDockAllocation) {
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

    override fun list(): MutableList<CappedCrossDockAllocation> {
        val out = mutableListOf<CappedCrossDockAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCrossDockAllocationStore(): CappedCrossDockAllocationStore {
    return MemoryCappedCrossDockAllocationStore(
        mutableMapOf<String, CappedCrossDockAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCrossDockAllocationStore(
    var inner: CappedCrossDockAllocationStore,
    var cache: TtlCache<CappedCrossDockAllocation>,
    var hits: Int,
    var misses: Int,
) : CappedCrossDockAllocationStore {
    override fun find(code: String): CappedCrossDockAllocation? {
        val hit = (cache.get(code) as CappedCrossDockAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCrossDockAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCrossDockAllocation> = inner.list()
}

fun newCachedCappedCrossDockAllocationStore(inner: CappedCrossDockAllocationStore, ttl: Int, clock: Clock): CappedCrossDockAllocationStore {
    return CachedCappedCrossDockAllocationStore(
        inner = inner,
        cache = newTtlCache<CappedCrossDockAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped cross dock allocations. Empty fields match all. */
data class CappedCrossDockAllocationQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCrossDockAllocation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedCrossDockAllocation>): MutableList<CappedCrossDockAllocation> {
        val matched = filter(items) { candidate: CappedCrossDockAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCrossDockAllocations(store: CappedCrossDockAllocationStore, query: CappedCrossDockAllocationQuery): MutableList<CappedCrossDockAllocation> {
    return query.apply(store.list())
}
