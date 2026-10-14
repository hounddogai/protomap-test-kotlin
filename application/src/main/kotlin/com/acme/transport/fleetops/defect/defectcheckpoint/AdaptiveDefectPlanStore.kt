package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive defect plans by code. */
interface AdaptiveDefectPlanStore {
    fun find(code: String): AdaptiveDefectPlan?
    fun save(item: AdaptiveDefectPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDefectPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDefectPlanStore(
    var items: MutableMap<String, AdaptiveDefectPlan>,
    var order: MutableList<String>,
) : AdaptiveDefectPlanStore {
    override fun find(code: String): AdaptiveDefectPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDefectPlan) {
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

    override fun list(): MutableList<AdaptiveDefectPlan> {
        val out = mutableListOf<AdaptiveDefectPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDefectPlanStore(): AdaptiveDefectPlanStore {
    return MemoryAdaptiveDefectPlanStore(mutableMapOf<String, AdaptiveDefectPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDefectPlanStore(
    var inner: AdaptiveDefectPlanStore,
    var cache: TtlCache<AdaptiveDefectPlan>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDefectPlanStore {
    override fun find(code: String): AdaptiveDefectPlan? {
        val hit = (cache.get(code) as AdaptiveDefectPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDefectPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDefectPlan> = inner.list()
}

fun newCachedAdaptiveDefectPlanStore(inner: AdaptiveDefectPlanStore, ttl: Int, clock: Clock): AdaptiveDefectPlanStore {
    return CachedAdaptiveDefectPlanStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDefectPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive defect plans. Empty fields match all. */
data class AdaptiveDefectPlanQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDefectPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDefectPlan>): MutableList<AdaptiveDefectPlan> {
        val matched = filter(items) { candidate: AdaptiveDefectPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDefectPlans(store: AdaptiveDefectPlanStore, query: AdaptiveDefectPlanQuery): MutableList<AdaptiveDefectPlan> {
    return query.apply(store.list())
}
