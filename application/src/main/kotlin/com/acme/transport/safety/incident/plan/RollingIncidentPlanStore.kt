package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling incident plans by code. */
interface RollingIncidentPlanStore {
    fun find(code: String): RollingIncidentPlan?
    fun save(item: RollingIncidentPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingIncidentPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingIncidentPlanStore(
    var items: MutableMap<String, RollingIncidentPlan>,
    var order: MutableList<String>,
) : RollingIncidentPlanStore {
    override fun find(code: String): RollingIncidentPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingIncidentPlan) {
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

    override fun list(): MutableList<RollingIncidentPlan> {
        val out = mutableListOf<RollingIncidentPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingIncidentPlanStore(): RollingIncidentPlanStore {
    return MemoryRollingIncidentPlanStore(mutableMapOf<String, RollingIncidentPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingIncidentPlanStore(
    var inner: RollingIncidentPlanStore,
    var cache: TtlCache<RollingIncidentPlan>,
    var hits: Int,
    var misses: Int,
) : RollingIncidentPlanStore {
    override fun find(code: String): RollingIncidentPlan? {
        val hit = (cache.get(code) as RollingIncidentPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingIncidentPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingIncidentPlan> = inner.list()
}

fun newCachedRollingIncidentPlanStore(inner: RollingIncidentPlanStore, ttl: Int, clock: Clock): RollingIncidentPlanStore {
    return CachedRollingIncidentPlanStore(
        inner = inner,
        cache = newTtlCache<RollingIncidentPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling incident plans. Empty fields match all. */
data class RollingIncidentPlanQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingIncidentPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingIncidentPlan>): MutableList<RollingIncidentPlan> {
        val matched = filter(items) { candidate: RollingIncidentPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingIncidentPlans(store: RollingIncidentPlanStore, query: RollingIncidentPlanQuery): MutableList<RollingIncidentPlan> {
    return query.apply(store.list())
}
