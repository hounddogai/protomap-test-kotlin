package com.acme.transport.safety.scorecard.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global scorecard budgets by code. */
interface GlobalScorecardBudgetStore {
    fun find(code: String): GlobalScorecardBudget?
    fun save(item: GlobalScorecardBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalScorecardBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalScorecardBudgetStore(
    var items: MutableMap<String, GlobalScorecardBudget>,
    var order: MutableList<String>,
) : GlobalScorecardBudgetStore {
    override fun find(code: String): GlobalScorecardBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalScorecardBudget) {
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

    override fun list(): MutableList<GlobalScorecardBudget> {
        val out = mutableListOf<GlobalScorecardBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalScorecardBudgetStore(): GlobalScorecardBudgetStore {
    return MemoryGlobalScorecardBudgetStore(
        mutableMapOf<String, GlobalScorecardBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalScorecardBudgetStore(
    var inner: GlobalScorecardBudgetStore,
    var cache: TtlCache<GlobalScorecardBudget>,
    var hits: Int,
    var misses: Int,
) : GlobalScorecardBudgetStore {
    override fun find(code: String): GlobalScorecardBudget? {
        val hit = (cache.get(code) as GlobalScorecardBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalScorecardBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalScorecardBudget> = inner.list()
}

fun newCachedGlobalScorecardBudgetStore(inner: GlobalScorecardBudgetStore, ttl: Int, clock: Clock): GlobalScorecardBudgetStore {
    return CachedGlobalScorecardBudgetStore(
        inner = inner,
        cache = newTtlCache<GlobalScorecardBudget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global scorecard budgets. Empty fields match all. */
data class GlobalScorecardBudgetQuery(
    var locale: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalScorecardBudget): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalScorecardBudget>): MutableList<GlobalScorecardBudget> {
        val matched = filter(items) { candidate: GlobalScorecardBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalScorecardBudgets(store: GlobalScorecardBudgetStore, query: GlobalScorecardBudgetQuery): MutableList<GlobalScorecardBudget> {
    return query.apply(store.list())
}
