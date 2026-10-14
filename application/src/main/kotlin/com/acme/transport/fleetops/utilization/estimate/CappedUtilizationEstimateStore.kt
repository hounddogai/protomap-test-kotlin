package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped utilization estimates by code. */
interface CappedUtilizationEstimateStore {
    fun find(code: String): CappedUtilizationEstimate?
    fun save(item: CappedUtilizationEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedUtilizationEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedUtilizationEstimateStore(
    var items: MutableMap<String, CappedUtilizationEstimate>,
    var order: MutableList<String>,
) : CappedUtilizationEstimateStore {
    override fun find(code: String): CappedUtilizationEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedUtilizationEstimate) {
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

    override fun list(): MutableList<CappedUtilizationEstimate> {
        val out = mutableListOf<CappedUtilizationEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedUtilizationEstimateStore(): CappedUtilizationEstimateStore {
    return MemoryCappedUtilizationEstimateStore(
        mutableMapOf<String, CappedUtilizationEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedUtilizationEstimateStore(
    var inner: CappedUtilizationEstimateStore,
    var cache: TtlCache<CappedUtilizationEstimate>,
    var hits: Int,
    var misses: Int,
) : CappedUtilizationEstimateStore {
    override fun find(code: String): CappedUtilizationEstimate? {
        val hit = (cache.get(code) as CappedUtilizationEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedUtilizationEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedUtilizationEstimate> = inner.list()
}

fun newCachedCappedUtilizationEstimateStore(inner: CappedUtilizationEstimateStore, ttl: Int, clock: Clock): CappedUtilizationEstimateStore {
    return CachedCappedUtilizationEstimateStore(
        inner = inner,
        cache = newTtlCache<CappedUtilizationEstimate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped utilization estimates. Empty fields match all. */
data class CappedUtilizationEstimateQuery(
    var category: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedUtilizationEstimate): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedUtilizationEstimate>): MutableList<CappedUtilizationEstimate> {
        val matched = filter(items) { candidate: CappedUtilizationEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedUtilizationEstimates(store: CappedUtilizationEstimateStore, query: CappedUtilizationEstimateQuery): MutableList<CappedUtilizationEstimate> {
    return query.apply(store.list())
}
