package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled fuel burn plans by code. */
interface ScheduledFuelBurnPlanStore {
    fun find(code: String): ScheduledFuelBurnPlan?
    fun save(item: ScheduledFuelBurnPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledFuelBurnPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledFuelBurnPlanStore(
    var items: MutableMap<String, ScheduledFuelBurnPlan>,
    var order: MutableList<String>,
) : ScheduledFuelBurnPlanStore {
    override fun find(code: String): ScheduledFuelBurnPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledFuelBurnPlan) {
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

    override fun list(): MutableList<ScheduledFuelBurnPlan> {
        val out = mutableListOf<ScheduledFuelBurnPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledFuelBurnPlanStore(): ScheduledFuelBurnPlanStore {
    return MemoryScheduledFuelBurnPlanStore(
        mutableMapOf<String, ScheduledFuelBurnPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledFuelBurnPlanStore(
    var inner: ScheduledFuelBurnPlanStore,
    var cache: TtlCache<ScheduledFuelBurnPlan>,
    var hits: Int,
    var misses: Int,
) : ScheduledFuelBurnPlanStore {
    override fun find(code: String): ScheduledFuelBurnPlan? {
        val hit = (cache.get(code) as ScheduledFuelBurnPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledFuelBurnPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledFuelBurnPlan> = inner.list()
}

fun newCachedScheduledFuelBurnPlanStore(inner: ScheduledFuelBurnPlanStore, ttl: Int, clock: Clock): ScheduledFuelBurnPlanStore {
    return CachedScheduledFuelBurnPlanStore(
        inner = inner,
        cache = newTtlCache<ScheduledFuelBurnPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled fuel burn plans. Empty fields match all. */
data class ScheduledFuelBurnPlanQuery(
    var source: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledFuelBurnPlan): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledFuelBurnPlan>): MutableList<ScheduledFuelBurnPlan> {
        val matched = filter(items) { candidate: ScheduledFuelBurnPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledFuelBurnPlans(store: ScheduledFuelBurnPlanStore, query: ScheduledFuelBurnPlanQuery): MutableList<ScheduledFuelBurnPlan> {
    return query.apply(store.list())
}
