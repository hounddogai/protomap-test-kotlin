package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted inspection plans by code. */
interface WeightedInspectionPlanStore {
    fun find(code: String): WeightedInspectionPlan?
    fun save(item: WeightedInspectionPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedInspectionPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedInspectionPlanStore(
    var items: MutableMap<String, WeightedInspectionPlan>,
    var order: MutableList<String>,
) : WeightedInspectionPlanStore {
    override fun find(code: String): WeightedInspectionPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedInspectionPlan) {
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

    override fun list(): MutableList<WeightedInspectionPlan> {
        val out = mutableListOf<WeightedInspectionPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedInspectionPlanStore(): WeightedInspectionPlanStore {
    return MemoryWeightedInspectionPlanStore(
        mutableMapOf<String, WeightedInspectionPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedInspectionPlanStore(
    var inner: WeightedInspectionPlanStore,
    var cache: TtlCache<WeightedInspectionPlan>,
    var hits: Int,
    var misses: Int,
) : WeightedInspectionPlanStore {
    override fun find(code: String): WeightedInspectionPlan? {
        val hit = (cache.get(code) as WeightedInspectionPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedInspectionPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedInspectionPlan> = inner.list()
}

fun newCachedWeightedInspectionPlanStore(inner: WeightedInspectionPlanStore, ttl: Int, clock: Clock): WeightedInspectionPlanStore {
    return CachedWeightedInspectionPlanStore(
        inner = inner,
        cache = newTtlCache<WeightedInspectionPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted inspection plans. Empty fields match all. */
data class WeightedInspectionPlanQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedInspectionPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedInspectionPlan>): MutableList<WeightedInspectionPlan> {
        val matched = filter(items) { candidate: WeightedInspectionPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedInspectionPlans(store: WeightedInspectionPlanStore, query: WeightedInspectionPlanQuery): MutableList<WeightedInspectionPlan> {
    return query.apply(store.list())
}
