package com.acme.transport.crewing.roster.plan

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending roster plans by code. */
interface PendingRosterPlanStore {
    fun find(code: String): PendingRosterPlan?
    fun save(item: PendingRosterPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRosterPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRosterPlanStore(
    var items: MutableMap<String, PendingRosterPlan>,
    var order: MutableList<String>,
) : PendingRosterPlanStore {
    override fun find(code: String): PendingRosterPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRosterPlan) {
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

    override fun list(): MutableList<PendingRosterPlan> {
        val out = mutableListOf<PendingRosterPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRosterPlanStore(): PendingRosterPlanStore {
    return MemoryPendingRosterPlanStore(mutableMapOf<String, PendingRosterPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRosterPlanStore(
    var inner: PendingRosterPlanStore,
    var cache: TtlCache<PendingRosterPlan>,
    var hits: Int,
    var misses: Int,
) : PendingRosterPlanStore {
    override fun find(code: String): PendingRosterPlan? {
        val hit = (cache.get(code) as PendingRosterPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRosterPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRosterPlan> = inner.list()
}

fun newCachedPendingRosterPlanStore(inner: PendingRosterPlanStore, ttl: Int, clock: Clock): PendingRosterPlanStore {
    return CachedPendingRosterPlanStore(
        inner = inner,
        cache = newTtlCache<PendingRosterPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending roster plans. Empty fields match all. */
data class PendingRosterPlanQuery(
    var country: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRosterPlan): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingRosterPlan>): MutableList<PendingRosterPlan> {
        val matched = filter(items) { candidate: PendingRosterPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRosterPlans(store: PendingRosterPlanStore, query: PendingRosterPlanQuery): MutableList<PendingRosterPlan> {
    return query.apply(store.list())
}
