package com.acme.transport.fleetops.defect.claim

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect plans by code. */
interface DefectPlanStore {
    fun find(code: String): DefectPlan?
    fun save(item: DefectPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectPlanStore(
    var items: MutableMap<String, DefectPlan>,
    var order: MutableList<String>,
) : DefectPlanStore {
    override fun find(code: String): DefectPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectPlan) {
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

    override fun list(): MutableList<DefectPlan> {
        val out = mutableListOf<DefectPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectPlanStore(): DefectPlanStore {
    return MemoryDefectPlanStore(mutableMapOf<String, DefectPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectPlanStore(
    var inner: DefectPlanStore,
    var cache: TtlCache<DefectPlan>,
    var hits: Int,
    var misses: Int,
) : DefectPlanStore {
    override fun find(code: String): DefectPlan? {
        val hit = (cache.get(code) as DefectPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectPlan> = inner.list()
}

fun newCachedDefectPlanStore(inner: DefectPlanStore, ttl: Int, clock: Clock): DefectPlanStore {
    return CachedDefectPlanStore(
        inner = inner,
        cache = newTtlCache<DefectPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect plans. Empty fields match all. */
data class DefectPlanQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<DefectPlan>): MutableList<DefectPlan> {
        val matched = filter(items) { candidate: DefectPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectPlans(store: DefectPlanStore, query: DefectPlanQuery): MutableList<DefectPlan> {
    return query.apply(store.list())
}
