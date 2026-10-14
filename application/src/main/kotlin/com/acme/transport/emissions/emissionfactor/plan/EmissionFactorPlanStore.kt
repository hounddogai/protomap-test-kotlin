package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor plans by code. */
interface EmissionFactorPlanStore {
    fun find(code: String): EmissionFactorPlan?
    fun save(item: EmissionFactorPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorPlanStore(
    var items: MutableMap<String, EmissionFactorPlan>,
    var order: MutableList<String>,
) : EmissionFactorPlanStore {
    override fun find(code: String): EmissionFactorPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorPlan) {
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

    override fun list(): MutableList<EmissionFactorPlan> {
        val out = mutableListOf<EmissionFactorPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorPlanStore(): EmissionFactorPlanStore {
    return MemoryEmissionFactorPlanStore(mutableMapOf<String, EmissionFactorPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorPlanStore(
    var inner: EmissionFactorPlanStore,
    var cache: TtlCache<EmissionFactorPlan>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorPlanStore {
    override fun find(code: String): EmissionFactorPlan? {
        val hit = (cache.get(code) as EmissionFactorPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorPlan> = inner.list()
}

fun newCachedEmissionFactorPlanStore(inner: EmissionFactorPlanStore, ttl: Int, clock: Clock): EmissionFactorPlanStore {
    return CachedEmissionFactorPlanStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor plans. Empty fields match all. */
data class EmissionFactorPlanQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorPlan>): MutableList<EmissionFactorPlan> {
        val matched = filter(items) { candidate: EmissionFactorPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorPlans(store: EmissionFactorPlanStore, query: EmissionFactorPlanQuery): MutableList<EmissionFactorPlan> {
    return query.apply(store.list())
}
