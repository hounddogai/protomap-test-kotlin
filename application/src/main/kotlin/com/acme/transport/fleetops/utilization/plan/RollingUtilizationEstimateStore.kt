package com.acme.transport.fleetops.utilization.plan

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling utilization estimates by code. */
interface RollingUtilizationEstimateStore {
    fun find(code: String): RollingUtilizationEstimate?
    fun save(item: RollingUtilizationEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingUtilizationEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingUtilizationEstimateStore(
    var items: MutableMap<String, RollingUtilizationEstimate>,
    var order: MutableList<String>,
) : RollingUtilizationEstimateStore {
    override fun find(code: String): RollingUtilizationEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingUtilizationEstimate) {
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

    override fun list(): MutableList<RollingUtilizationEstimate> {
        val out = mutableListOf<RollingUtilizationEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingUtilizationEstimateStore(): RollingUtilizationEstimateStore {
    return MemoryRollingUtilizationEstimateStore(
        mutableMapOf<String, RollingUtilizationEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingUtilizationEstimateStore(
    var inner: RollingUtilizationEstimateStore,
    var cache: TtlCache<RollingUtilizationEstimate>,
    var hits: Int,
    var misses: Int,
) : RollingUtilizationEstimateStore {
    override fun find(code: String): RollingUtilizationEstimate? {
        val hit = (cache.get(code) as RollingUtilizationEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingUtilizationEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingUtilizationEstimate> = inner.list()
}

fun newCachedRollingUtilizationEstimateStore(inner: RollingUtilizationEstimateStore, ttl: Int, clock: Clock): RollingUtilizationEstimateStore {
    return CachedRollingUtilizationEstimateStore(
        inner = inner,
        cache = newTtlCache<RollingUtilizationEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling utilization estimates. Empty fields match all. */
data class RollingUtilizationEstimateQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingUtilizationEstimate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingUtilizationEstimate>): MutableList<RollingUtilizationEstimate> {
        val matched = filter(items) { candidate: RollingUtilizationEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingUtilizationEstimates(store: RollingUtilizationEstimateStore, query: RollingUtilizationEstimateQuery): MutableList<RollingUtilizationEstimate> {
    return query.apply(store.list())
}
