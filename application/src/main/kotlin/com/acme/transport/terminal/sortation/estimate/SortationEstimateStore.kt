package com.acme.transport.terminal.sortation.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation estimates by code. */
interface SortationEstimateStore {
    fun find(code: String): SortationEstimate?
    fun save(item: SortationEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationEstimateStore(
    var items: MutableMap<String, SortationEstimate>,
    var order: MutableList<String>,
) : SortationEstimateStore {
    override fun find(code: String): SortationEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationEstimate) {
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

    override fun list(): MutableList<SortationEstimate> {
        val out = mutableListOf<SortationEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationEstimateStore(): SortationEstimateStore {
    return MemorySortationEstimateStore(mutableMapOf<String, SortationEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationEstimateStore(
    var inner: SortationEstimateStore,
    var cache: TtlCache<SortationEstimate>,
    var hits: Int,
    var misses: Int,
) : SortationEstimateStore {
    override fun find(code: String): SortationEstimate? {
        val hit = (cache.get(code) as SortationEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationEstimate> = inner.list()
}

fun newCachedSortationEstimateStore(inner: SortationEstimateStore, ttl: Int, clock: Clock): SortationEstimateStore {
    return CachedSortationEstimateStore(
        inner = inner,
        cache = newTtlCache<SortationEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation estimates. Empty fields match all. */
data class SortationEstimateQuery(
    var depot: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationEstimate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationEstimate>): MutableList<SortationEstimate> {
        val matched = filter(items) { candidate: SortationEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationEstimates(store: SortationEstimateStore, query: SortationEstimateQuery): MutableList<SortationEstimate> {
    return query.apply(store.list())
}
