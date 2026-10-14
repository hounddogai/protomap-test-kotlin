package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted hub plans by code. */
interface WeightedHubPlanStore {
    fun find(code: String): WeightedHubPlan?
    fun save(item: WeightedHubPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedHubPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedHubPlanStore(
    var items: MutableMap<String, WeightedHubPlan>,
    var order: MutableList<String>,
) : WeightedHubPlanStore {
    override fun find(code: String): WeightedHubPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedHubPlan) {
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

    override fun list(): MutableList<WeightedHubPlan> {
        val out = mutableListOf<WeightedHubPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedHubPlanStore(): WeightedHubPlanStore {
    return MemoryWeightedHubPlanStore(mutableMapOf<String, WeightedHubPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedHubPlanStore(
    var inner: WeightedHubPlanStore,
    var cache: TtlCache<WeightedHubPlan>,
    var hits: Int,
    var misses: Int,
) : WeightedHubPlanStore {
    override fun find(code: String): WeightedHubPlan? {
        val hit = (cache.get(code) as WeightedHubPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedHubPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedHubPlan> = inner.list()
}

fun newCachedWeightedHubPlanStore(inner: WeightedHubPlanStore, ttl: Int, clock: Clock): WeightedHubPlanStore {
    return CachedWeightedHubPlanStore(
        inner = inner,
        cache = newTtlCache<WeightedHubPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted hub plans. Empty fields match all. */
data class WeightedHubPlanQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedHubPlan): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedHubPlan>): MutableList<WeightedHubPlan> {
        val matched = filter(items) { candidate: WeightedHubPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedHubPlans(store: WeightedHubPlanStore, query: WeightedHubPlanQuery): MutableList<WeightedHubPlan> {
    return query.apply(store.list())
}
