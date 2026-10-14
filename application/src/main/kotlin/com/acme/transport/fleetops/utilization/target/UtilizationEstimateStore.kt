package com.acme.transport.fleetops.utilization.target

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization estimates by code. */
interface UtilizationEstimateStore {
    fun find(code: String): UtilizationEstimate?
    fun save(item: UtilizationEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationEstimateStore(
    var items: MutableMap<String, UtilizationEstimate>,
    var order: MutableList<String>,
) : UtilizationEstimateStore {
    override fun find(code: String): UtilizationEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationEstimate) {
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

    override fun list(): MutableList<UtilizationEstimate> {
        val out = mutableListOf<UtilizationEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationEstimateStore(): UtilizationEstimateStore {
    return MemoryUtilizationEstimateStore(mutableMapOf<String, UtilizationEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationEstimateStore(
    var inner: UtilizationEstimateStore,
    var cache: TtlCache<UtilizationEstimate>,
    var hits: Int,
    var misses: Int,
) : UtilizationEstimateStore {
    override fun find(code: String): UtilizationEstimate? {
        val hit = (cache.get(code) as UtilizationEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationEstimate> = inner.list()
}

fun newCachedUtilizationEstimateStore(inner: UtilizationEstimateStore, ttl: Int, clock: Clock): UtilizationEstimateStore {
    return CachedUtilizationEstimateStore(
        inner = inner,
        cache = newTtlCache<UtilizationEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization estimates. Empty fields match all. */
data class UtilizationEstimateQuery(
    var depot: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationEstimate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationEstimate>): MutableList<UtilizationEstimate> {
        val matched = filter(items) { candidate: UtilizationEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationEstimates(store: UtilizationEstimateStore, query: UtilizationEstimateQuery): MutableList<UtilizationEstimate> {
    return query.apply(store.list())
}
