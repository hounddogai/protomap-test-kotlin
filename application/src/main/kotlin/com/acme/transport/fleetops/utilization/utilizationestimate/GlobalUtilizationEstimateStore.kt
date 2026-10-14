package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global utilization estimates by code. */
interface GlobalUtilizationEstimateStore {
    fun find(code: String): GlobalUtilizationEstimate?
    fun save(item: GlobalUtilizationEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalUtilizationEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalUtilizationEstimateStore(
    var items: MutableMap<String, GlobalUtilizationEstimate>,
    var order: MutableList<String>,
) : GlobalUtilizationEstimateStore {
    override fun find(code: String): GlobalUtilizationEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalUtilizationEstimate) {
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

    override fun list(): MutableList<GlobalUtilizationEstimate> {
        val out = mutableListOf<GlobalUtilizationEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalUtilizationEstimateStore(): GlobalUtilizationEstimateStore {
    return MemoryGlobalUtilizationEstimateStore(
        mutableMapOf<String, GlobalUtilizationEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalUtilizationEstimateStore(
    var inner: GlobalUtilizationEstimateStore,
    var cache: TtlCache<GlobalUtilizationEstimate>,
    var hits: Int,
    var misses: Int,
) : GlobalUtilizationEstimateStore {
    override fun find(code: String): GlobalUtilizationEstimate? {
        val hit = (cache.get(code) as GlobalUtilizationEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalUtilizationEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalUtilizationEstimate> = inner.list()
}

fun newCachedGlobalUtilizationEstimateStore(inner: GlobalUtilizationEstimateStore, ttl: Int, clock: Clock): GlobalUtilizationEstimateStore {
    return CachedGlobalUtilizationEstimateStore(
        inner = inner,
        cache = newTtlCache<GlobalUtilizationEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global utilization estimates. Empty fields match all. */
data class GlobalUtilizationEstimateQuery(
    var country: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalUtilizationEstimate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalUtilizationEstimate>): MutableList<GlobalUtilizationEstimate> {
        val matched = filter(items) { candidate: GlobalUtilizationEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalUtilizationEstimates(store: GlobalUtilizationEstimateStore, query: GlobalUtilizationEstimateQuery): MutableList<GlobalUtilizationEstimate> {
    return query.apply(store.list())
}
