package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default defect plans by code. */
interface DefaultDefectPlanStore {
    fun find(code: String): DefaultDefectPlan?
    fun save(item: DefaultDefectPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDefectPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDefectPlanStore(
    var items: MutableMap<String, DefaultDefectPlan>,
    var order: MutableList<String>,
) : DefaultDefectPlanStore {
    override fun find(code: String): DefaultDefectPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDefectPlan) {
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

    override fun list(): MutableList<DefaultDefectPlan> {
        val out = mutableListOf<DefaultDefectPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDefectPlanStore(): DefaultDefectPlanStore {
    return MemoryDefaultDefectPlanStore(mutableMapOf<String, DefaultDefectPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDefectPlanStore(
    var inner: DefaultDefectPlanStore,
    var cache: TtlCache<DefaultDefectPlan>,
    var hits: Int,
    var misses: Int,
) : DefaultDefectPlanStore {
    override fun find(code: String): DefaultDefectPlan? {
        val hit = (cache.get(code) as DefaultDefectPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDefectPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDefectPlan> = inner.list()
}

fun newCachedDefaultDefectPlanStore(inner: DefaultDefectPlanStore, ttl: Int, clock: Clock): DefaultDefectPlanStore {
    return CachedDefaultDefectPlanStore(
        inner = inner,
        cache = newTtlCache<DefaultDefectPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default defect plans. Empty fields match all. */
data class DefaultDefectPlanQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDefectPlan): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultDefectPlan>): MutableList<DefaultDefectPlan> {
        val matched = filter(items) { candidate: DefaultDefectPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDefectPlans(store: DefaultDefectPlanStore, query: DefaultDefectPlanQuery): MutableList<DefaultDefectPlan> {
    return query.apply(store.list())
}
