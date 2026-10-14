package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual inspection plans by code. */
interface ManualInspectionPlanStore {
    fun find(code: String): ManualInspectionPlan?
    fun save(item: ManualInspectionPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualInspectionPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualInspectionPlanStore(
    var items: MutableMap<String, ManualInspectionPlan>,
    var order: MutableList<String>,
) : ManualInspectionPlanStore {
    override fun find(code: String): ManualInspectionPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualInspectionPlan) {
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

    override fun list(): MutableList<ManualInspectionPlan> {
        val out = mutableListOf<ManualInspectionPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualInspectionPlanStore(): ManualInspectionPlanStore {
    return MemoryManualInspectionPlanStore(
        mutableMapOf<String, ManualInspectionPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualInspectionPlanStore(
    var inner: ManualInspectionPlanStore,
    var cache: TtlCache<ManualInspectionPlan>,
    var hits: Int,
    var misses: Int,
) : ManualInspectionPlanStore {
    override fun find(code: String): ManualInspectionPlan? {
        val hit = (cache.get(code) as ManualInspectionPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualInspectionPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualInspectionPlan> = inner.list()
}

fun newCachedManualInspectionPlanStore(inner: ManualInspectionPlanStore, ttl: Int, clock: Clock): ManualInspectionPlanStore {
    return CachedManualInspectionPlanStore(
        inner = inner,
        cache = newTtlCache<ManualInspectionPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual inspection plans. Empty fields match all. */
data class ManualInspectionPlanQuery(
    var category: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualInspectionPlan): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualInspectionPlan>): MutableList<ManualInspectionPlan> {
        val matched = filter(items) { candidate: ManualInspectionPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualInspectionPlans(store: ManualInspectionPlanStore, query: ManualInspectionPlanQuery): MutableList<ManualInspectionPlan> {
    return query.apply(store.list())
}
