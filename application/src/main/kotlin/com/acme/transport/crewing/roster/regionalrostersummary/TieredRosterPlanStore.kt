package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered roster plans by code. */
interface TieredRosterPlanStore {
    fun find(code: String): TieredRosterPlan?
    fun save(item: TieredRosterPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRosterPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRosterPlanStore(
    var items: MutableMap<String, TieredRosterPlan>,
    var order: MutableList<String>,
) : TieredRosterPlanStore {
    override fun find(code: String): TieredRosterPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRosterPlan) {
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

    override fun list(): MutableList<TieredRosterPlan> {
        val out = mutableListOf<TieredRosterPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRosterPlanStore(): TieredRosterPlanStore {
    return MemoryTieredRosterPlanStore(mutableMapOf<String, TieredRosterPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRosterPlanStore(
    var inner: TieredRosterPlanStore,
    var cache: TtlCache<TieredRosterPlan>,
    var hits: Int,
    var misses: Int,
) : TieredRosterPlanStore {
    override fun find(code: String): TieredRosterPlan? {
        val hit = (cache.get(code) as TieredRosterPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRosterPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRosterPlan> = inner.list()
}

fun newCachedTieredRosterPlanStore(inner: TieredRosterPlanStore, ttl: Int, clock: Clock): TieredRosterPlanStore {
    return CachedTieredRosterPlanStore(
        inner = inner,
        cache = newTtlCache<TieredRosterPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered roster plans. Empty fields match all. */
data class TieredRosterPlanQuery(
    var depot: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRosterPlan): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredRosterPlan>): MutableList<TieredRosterPlan> {
        val matched = filter(items) { candidate: TieredRosterPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRosterPlans(store: TieredRosterPlanStore, query: TieredRosterPlanQuery): MutableList<TieredRosterPlan> {
    return query.apply(store.list())
}
