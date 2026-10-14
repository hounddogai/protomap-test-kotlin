package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break allocations by code. */
interface RestBreakAllocationStore {
    fun find(code: String): RestBreakAllocation?
    fun save(item: RestBreakAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakAllocationStore(
    var items: MutableMap<String, RestBreakAllocation>,
    var order: MutableList<String>,
) : RestBreakAllocationStore {
    override fun find(code: String): RestBreakAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakAllocation) {
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

    override fun list(): MutableList<RestBreakAllocation> {
        val out = mutableListOf<RestBreakAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakAllocationStore(): RestBreakAllocationStore {
    return MemoryRestBreakAllocationStore(mutableMapOf<String, RestBreakAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakAllocationStore(
    var inner: RestBreakAllocationStore,
    var cache: TtlCache<RestBreakAllocation>,
    var hits: Int,
    var misses: Int,
) : RestBreakAllocationStore {
    override fun find(code: String): RestBreakAllocation? {
        val hit = (cache.get(code) as RestBreakAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakAllocation> = inner.list()
}

fun newCachedRestBreakAllocationStore(inner: RestBreakAllocationStore, ttl: Int, clock: Clock): RestBreakAllocationStore {
    return CachedRestBreakAllocationStore(
        inner = inner,
        cache = newTtlCache<RestBreakAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break allocations. Empty fields match all. */
data class RestBreakAllocationQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakAllocation>): MutableList<RestBreakAllocation> {
        val matched = filter(items) { candidate: RestBreakAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakAllocations(store: RestBreakAllocationStore, query: RestBreakAllocationQuery): MutableList<RestBreakAllocation> {
    return query.apply(store.list())
}
