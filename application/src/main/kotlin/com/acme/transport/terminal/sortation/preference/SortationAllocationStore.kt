package com.acme.transport.terminal.sortation.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation allocations by code. */
interface SortationAllocationStore {
    fun find(code: String): SortationAllocation?
    fun save(item: SortationAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationAllocationStore(
    var items: MutableMap<String, SortationAllocation>,
    var order: MutableList<String>,
) : SortationAllocationStore {
    override fun find(code: String): SortationAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationAllocation) {
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

    override fun list(): MutableList<SortationAllocation> {
        val out = mutableListOf<SortationAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationAllocationStore(): SortationAllocationStore {
    return MemorySortationAllocationStore(mutableMapOf<String, SortationAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationAllocationStore(
    var inner: SortationAllocationStore,
    var cache: TtlCache<SortationAllocation>,
    var hits: Int,
    var misses: Int,
) : SortationAllocationStore {
    override fun find(code: String): SortationAllocation? {
        val hit = (cache.get(code) as SortationAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationAllocation> = inner.list()
}

fun newCachedSortationAllocationStore(inner: SortationAllocationStore, ttl: Int, clock: Clock): SortationAllocationStore {
    return CachedSortationAllocationStore(
        inner = inner,
        cache = newTtlCache<SortationAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation allocations. Empty fields match all. */
data class SortationAllocationQuery(
    var locale: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationAllocation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<SortationAllocation>): MutableList<SortationAllocation> {
        val matched = filter(items) { candidate: SortationAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationAllocations(store: SortationAllocationStore, query: SortationAllocationQuery): MutableList<SortationAllocation> {
    return query.apply(store.list())
}
