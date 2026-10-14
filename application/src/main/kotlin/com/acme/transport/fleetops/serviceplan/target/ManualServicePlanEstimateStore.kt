package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual service plan estimates by code. */
interface ManualServicePlanEstimateStore {
    fun find(code: String): ManualServicePlanEstimate?
    fun save(item: ManualServicePlanEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualServicePlanEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualServicePlanEstimateStore(
    var items: MutableMap<String, ManualServicePlanEstimate>,
    var order: MutableList<String>,
) : ManualServicePlanEstimateStore {
    override fun find(code: String): ManualServicePlanEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualServicePlanEstimate) {
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

    override fun list(): MutableList<ManualServicePlanEstimate> {
        val out = mutableListOf<ManualServicePlanEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualServicePlanEstimateStore(): ManualServicePlanEstimateStore {
    return MemoryManualServicePlanEstimateStore(
        mutableMapOf<String, ManualServicePlanEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualServicePlanEstimateStore(
    var inner: ManualServicePlanEstimateStore,
    var cache: TtlCache<ManualServicePlanEstimate>,
    var hits: Int,
    var misses: Int,
) : ManualServicePlanEstimateStore {
    override fun find(code: String): ManualServicePlanEstimate? {
        val hit = (cache.get(code) as ManualServicePlanEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualServicePlanEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualServicePlanEstimate> = inner.list()
}

fun newCachedManualServicePlanEstimateStore(inner: ManualServicePlanEstimateStore, ttl: Int, clock: Clock): ManualServicePlanEstimateStore {
    return CachedManualServicePlanEstimateStore(
        inner = inner,
        cache = newTtlCache<ManualServicePlanEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual service plan estimates. Empty fields match all. */
data class ManualServicePlanEstimateQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualServicePlanEstimate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualServicePlanEstimate>): MutableList<ManualServicePlanEstimate> {
        val matched = filter(items) { candidate: ManualServicePlanEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualServicePlanEstimates(store: ManualServicePlanEstimateStore, query: ManualServicePlanEstimateQuery): MutableList<ManualServicePlanEstimate> {
    return query.apply(store.list())
}
