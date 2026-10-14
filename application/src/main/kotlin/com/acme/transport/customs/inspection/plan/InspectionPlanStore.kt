package com.acme.transport.customs.inspection.plan

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection plans by code. */
interface InspectionPlanStore {
    fun find(code: String): InspectionPlan?
    fun save(item: InspectionPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionPlanStore(
    var items: MutableMap<String, InspectionPlan>,
    var order: MutableList<String>,
) : InspectionPlanStore {
    override fun find(code: String): InspectionPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionPlan) {
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

    override fun list(): MutableList<InspectionPlan> {
        val out = mutableListOf<InspectionPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionPlanStore(): InspectionPlanStore {
    return MemoryInspectionPlanStore(mutableMapOf<String, InspectionPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionPlanStore(
    var inner: InspectionPlanStore,
    var cache: TtlCache<InspectionPlan>,
    var hits: Int,
    var misses: Int,
) : InspectionPlanStore {
    override fun find(code: String): InspectionPlan? {
        val hit = (cache.get(code) as InspectionPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionPlan> = inner.list()
}

fun newCachedInspectionPlanStore(inner: InspectionPlanStore, ttl: Int, clock: Clock): InspectionPlanStore {
    return CachedInspectionPlanStore(
        inner = inner,
        cache = newTtlCache<InspectionPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection plans. Empty fields match all. */
data class InspectionPlanQuery(
    var label: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionPlan): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionPlan>): MutableList<InspectionPlan> {
        val matched = filter(items) { candidate: InspectionPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionPlans(store: InspectionPlanStore, query: InspectionPlanQuery): MutableList<InspectionPlan> {
    return query.apply(store.list())
}
