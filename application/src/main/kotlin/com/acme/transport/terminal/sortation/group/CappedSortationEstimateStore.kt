package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped sortation estimates by code. */
interface CappedSortationEstimateStore {
    fun find(code: String): CappedSortationEstimate?
    fun save(item: CappedSortationEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedSortationEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedSortationEstimateStore(
    var items: MutableMap<String, CappedSortationEstimate>,
    var order: MutableList<String>,
) : CappedSortationEstimateStore {
    override fun find(code: String): CappedSortationEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedSortationEstimate) {
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

    override fun list(): MutableList<CappedSortationEstimate> {
        val out = mutableListOf<CappedSortationEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedSortationEstimateStore(): CappedSortationEstimateStore {
    return MemoryCappedSortationEstimateStore(
        mutableMapOf<String, CappedSortationEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedSortationEstimateStore(
    var inner: CappedSortationEstimateStore,
    var cache: TtlCache<CappedSortationEstimate>,
    var hits: Int,
    var misses: Int,
) : CappedSortationEstimateStore {
    override fun find(code: String): CappedSortationEstimate? {
        val hit = (cache.get(code) as CappedSortationEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedSortationEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedSortationEstimate> = inner.list()
}

fun newCachedCappedSortationEstimateStore(inner: CappedSortationEstimateStore, ttl: Int, clock: Clock): CappedSortationEstimateStore {
    return CachedCappedSortationEstimateStore(
        inner = inner,
        cache = newTtlCache<CappedSortationEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped sortation estimates. Empty fields match all. */
data class CappedSortationEstimateQuery(
    var country: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedSortationEstimate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedSortationEstimate>): MutableList<CappedSortationEstimate> {
        val matched = filter(items) { candidate: CappedSortationEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedSortationEstimates(store: CappedSortationEstimateStore, query: CappedSortationEstimateQuery): MutableList<CappedSortationEstimate> {
    return query.apply(store.list())
}
