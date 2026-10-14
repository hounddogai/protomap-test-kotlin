package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered fuel burn plans by code. */
interface TieredFuelBurnPlanStore {
    fun find(code: String): TieredFuelBurnPlan?
    fun save(item: TieredFuelBurnPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredFuelBurnPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredFuelBurnPlanStore(
    var items: MutableMap<String, TieredFuelBurnPlan>,
    var order: MutableList<String>,
) : TieredFuelBurnPlanStore {
    override fun find(code: String): TieredFuelBurnPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredFuelBurnPlan) {
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

    override fun list(): MutableList<TieredFuelBurnPlan> {
        val out = mutableListOf<TieredFuelBurnPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredFuelBurnPlanStore(): TieredFuelBurnPlanStore {
    return MemoryTieredFuelBurnPlanStore(mutableMapOf<String, TieredFuelBurnPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredFuelBurnPlanStore(
    var inner: TieredFuelBurnPlanStore,
    var cache: TtlCache<TieredFuelBurnPlan>,
    var hits: Int,
    var misses: Int,
) : TieredFuelBurnPlanStore {
    override fun find(code: String): TieredFuelBurnPlan? {
        val hit = (cache.get(code) as TieredFuelBurnPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredFuelBurnPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredFuelBurnPlan> = inner.list()
}

fun newCachedTieredFuelBurnPlanStore(inner: TieredFuelBurnPlanStore, ttl: Int, clock: Clock): TieredFuelBurnPlanStore {
    return CachedTieredFuelBurnPlanStore(
        inner = inner,
        cache = newTtlCache<TieredFuelBurnPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered fuel burn plans. Empty fields match all. */
data class TieredFuelBurnPlanQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredFuelBurnPlan): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TieredFuelBurnPlan>): MutableList<TieredFuelBurnPlan> {
        val matched = filter(items) { candidate: TieredFuelBurnPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredFuelBurnPlans(store: TieredFuelBurnPlanStore, query: TieredFuelBurnPlanQuery): MutableList<TieredFuelBurnPlan> {
    return query.apply(store.list())
}
