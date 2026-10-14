package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident plans by code. */
interface IncidentPlanStore {
    fun find(code: String): IncidentPlan?
    fun save(item: IncidentPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentPlanStore(
    var items: MutableMap<String, IncidentPlan>,
    var order: MutableList<String>,
) : IncidentPlanStore {
    override fun find(code: String): IncidentPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentPlan) {
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

    override fun list(): MutableList<IncidentPlan> {
        val out = mutableListOf<IncidentPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentPlanStore(): IncidentPlanStore {
    return MemoryIncidentPlanStore(mutableMapOf<String, IncidentPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentPlanStore(
    var inner: IncidentPlanStore,
    var cache: TtlCache<IncidentPlan>,
    var hits: Int,
    var misses: Int,
) : IncidentPlanStore {
    override fun find(code: String): IncidentPlan? {
        val hit = (cache.get(code) as IncidentPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentPlan> = inner.list()
}

fun newCachedIncidentPlanStore(inner: IncidentPlanStore, ttl: Int, clock: Clock): IncidentPlanStore {
    return CachedIncidentPlanStore(
        inner = inner,
        cache = newTtlCache<IncidentPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident plans. Empty fields match all. */
data class IncidentPlanQuery(
    var country: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentPlan): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<IncidentPlan>): MutableList<IncidentPlan> {
        val matched = filter(items) { candidate: IncidentPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentPlans(store: IncidentPlanStore, query: IncidentPlanQuery): MutableList<IncidentPlan> {
    return query.apply(store.list())
}
