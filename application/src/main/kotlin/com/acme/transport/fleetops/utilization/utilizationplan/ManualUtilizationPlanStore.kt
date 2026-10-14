package com.acme.transport.fleetops.utilization.utilizationplan

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual utilization plans by code. */
interface ManualUtilizationPlanStore {
    fun find(code: String): ManualUtilizationPlan?
    fun save(item: ManualUtilizationPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualUtilizationPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualUtilizationPlanStore(
    var items: MutableMap<String, ManualUtilizationPlan>,
    var order: MutableList<String>,
) : ManualUtilizationPlanStore {
    override fun find(code: String): ManualUtilizationPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualUtilizationPlan) {
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

    override fun list(): MutableList<ManualUtilizationPlan> {
        val out = mutableListOf<ManualUtilizationPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualUtilizationPlanStore(): ManualUtilizationPlanStore {
    return MemoryManualUtilizationPlanStore(
        mutableMapOf<String, ManualUtilizationPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualUtilizationPlanStore(
    var inner: ManualUtilizationPlanStore,
    var cache: TtlCache<ManualUtilizationPlan>,
    var hits: Int,
    var misses: Int,
) : ManualUtilizationPlanStore {
    override fun find(code: String): ManualUtilizationPlan? {
        val hit = (cache.get(code) as ManualUtilizationPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualUtilizationPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualUtilizationPlan> = inner.list()
}

fun newCachedManualUtilizationPlanStore(inner: ManualUtilizationPlanStore, ttl: Int, clock: Clock): ManualUtilizationPlanStore {
    return CachedManualUtilizationPlanStore(
        inner = inner,
        cache = newTtlCache<ManualUtilizationPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual utilization plans. Empty fields match all. */
data class ManualUtilizationPlanQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualUtilizationPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualUtilizationPlan>): MutableList<ManualUtilizationPlan> {
        val matched = filter(items) { candidate: ManualUtilizationPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualUtilizationPlans(store: ManualUtilizationPlanStore, query: ManualUtilizationPlanQuery): MutableList<ManualUtilizationPlan> {
    return query.apply(store.list())
}
