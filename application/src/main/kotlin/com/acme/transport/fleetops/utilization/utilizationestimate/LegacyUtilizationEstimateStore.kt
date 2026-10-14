package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy utilization estimates by code. */
interface LegacyUtilizationEstimateStore {
    fun find(code: String): LegacyUtilizationEstimate?
    fun save(item: LegacyUtilizationEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyUtilizationEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyUtilizationEstimateStore(
    var items: MutableMap<String, LegacyUtilizationEstimate>,
    var order: MutableList<String>,
) : LegacyUtilizationEstimateStore {
    override fun find(code: String): LegacyUtilizationEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyUtilizationEstimate) {
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

    override fun list(): MutableList<LegacyUtilizationEstimate> {
        val out = mutableListOf<LegacyUtilizationEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyUtilizationEstimateStore(): LegacyUtilizationEstimateStore {
    return MemoryLegacyUtilizationEstimateStore(
        mutableMapOf<String, LegacyUtilizationEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyUtilizationEstimateStore(
    var inner: LegacyUtilizationEstimateStore,
    var cache: TtlCache<LegacyUtilizationEstimate>,
    var hits: Int,
    var misses: Int,
) : LegacyUtilizationEstimateStore {
    override fun find(code: String): LegacyUtilizationEstimate? {
        val hit = (cache.get(code) as LegacyUtilizationEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyUtilizationEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyUtilizationEstimate> = inner.list()
}

fun newCachedLegacyUtilizationEstimateStore(inner: LegacyUtilizationEstimateStore, ttl: Int, clock: Clock): LegacyUtilizationEstimateStore {
    return CachedLegacyUtilizationEstimateStore(
        inner = inner,
        cache = newTtlCache<LegacyUtilizationEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy utilization estimates. Empty fields match all. */
data class LegacyUtilizationEstimateQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyUtilizationEstimate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyUtilizationEstimate>): MutableList<LegacyUtilizationEstimate> {
        val matched = filter(items) { candidate: LegacyUtilizationEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyUtilizationEstimates(store: LegacyUtilizationEstimateStore, query: LegacyUtilizationEstimateQuery): MutableList<LegacyUtilizationEstimate> {
    return query.apply(store.list())
}
