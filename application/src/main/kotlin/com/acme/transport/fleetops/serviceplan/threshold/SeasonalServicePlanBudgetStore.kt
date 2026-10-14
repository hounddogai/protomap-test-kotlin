package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal service plan budgets by code. */
interface SeasonalServicePlanBudgetStore {
    fun find(code: String): SeasonalServicePlanBudget?
    fun save(item: SeasonalServicePlanBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalServicePlanBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalServicePlanBudgetStore(
    var items: MutableMap<String, SeasonalServicePlanBudget>,
    var order: MutableList<String>,
) : SeasonalServicePlanBudgetStore {
    override fun find(code: String): SeasonalServicePlanBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalServicePlanBudget) {
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

    override fun list(): MutableList<SeasonalServicePlanBudget> {
        val out = mutableListOf<SeasonalServicePlanBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalServicePlanBudgetStore(): SeasonalServicePlanBudgetStore {
    return MemorySeasonalServicePlanBudgetStore(
        mutableMapOf<String, SeasonalServicePlanBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalServicePlanBudgetStore(
    var inner: SeasonalServicePlanBudgetStore,
    var cache: TtlCache<SeasonalServicePlanBudget>,
    var hits: Int,
    var misses: Int,
) : SeasonalServicePlanBudgetStore {
    override fun find(code: String): SeasonalServicePlanBudget? {
        val hit = (cache.get(code) as SeasonalServicePlanBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalServicePlanBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalServicePlanBudget> = inner.list()
}

fun newCachedSeasonalServicePlanBudgetStore(inner: SeasonalServicePlanBudgetStore, ttl: Int, clock: Clock): SeasonalServicePlanBudgetStore {
    return CachedSeasonalServicePlanBudgetStore(
        inner = inner,
        cache = newTtlCache<SeasonalServicePlanBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal service plan budgets. Empty fields match all. */
data class SeasonalServicePlanBudgetQuery(
    var category: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalServicePlanBudget): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<SeasonalServicePlanBudget>): MutableList<SeasonalServicePlanBudget> {
        val matched = filter(items) { candidate: SeasonalServicePlanBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalServicePlanBudgets(store: SeasonalServicePlanBudgetStore, query: SeasonalServicePlanBudgetQuery): MutableList<SeasonalServicePlanBudget> {
    return query.apply(store.list())
}
