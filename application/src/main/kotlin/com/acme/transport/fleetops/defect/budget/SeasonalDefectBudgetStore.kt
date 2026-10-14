package com.acme.transport.fleetops.defect.budget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal defect budgets by code. */
interface SeasonalDefectBudgetStore {
    fun find(code: String): SeasonalDefectBudget?
    fun save(item: SeasonalDefectBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDefectBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDefectBudgetStore(
    var items: MutableMap<String, SeasonalDefectBudget>,
    var order: MutableList<String>,
) : SeasonalDefectBudgetStore {
    override fun find(code: String): SeasonalDefectBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDefectBudget) {
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

    override fun list(): MutableList<SeasonalDefectBudget> {
        val out = mutableListOf<SeasonalDefectBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDefectBudgetStore(): SeasonalDefectBudgetStore {
    return MemorySeasonalDefectBudgetStore(
        mutableMapOf<String, SeasonalDefectBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDefectBudgetStore(
    var inner: SeasonalDefectBudgetStore,
    var cache: TtlCache<SeasonalDefectBudget>,
    var hits: Int,
    var misses: Int,
) : SeasonalDefectBudgetStore {
    override fun find(code: String): SeasonalDefectBudget? {
        val hit = (cache.get(code) as SeasonalDefectBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDefectBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDefectBudget> = inner.list()
}

fun newCachedSeasonalDefectBudgetStore(inner: SeasonalDefectBudgetStore, ttl: Int, clock: Clock): SeasonalDefectBudgetStore {
    return CachedSeasonalDefectBudgetStore(
        inner = inner,
        cache = newTtlCache<SeasonalDefectBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal defect budgets. Empty fields match all. */
data class SeasonalDefectBudgetQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDefectBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDefectBudget>): MutableList<SeasonalDefectBudget> {
        val matched = filter(items) { candidate: SeasonalDefectBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDefectBudgets(store: SeasonalDefectBudgetStore, query: SeasonalDefectBudgetQuery): MutableList<SeasonalDefectBudget> {
    return query.apply(store.list())
}
