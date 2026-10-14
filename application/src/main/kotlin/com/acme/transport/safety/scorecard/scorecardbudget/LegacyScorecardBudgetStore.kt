package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy scorecard budgets by code. */
interface LegacyScorecardBudgetStore {
    fun find(code: String): LegacyScorecardBudget?
    fun save(item: LegacyScorecardBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyScorecardBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyScorecardBudgetStore(
    var items: MutableMap<String, LegacyScorecardBudget>,
    var order: MutableList<String>,
) : LegacyScorecardBudgetStore {
    override fun find(code: String): LegacyScorecardBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyScorecardBudget) {
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

    override fun list(): MutableList<LegacyScorecardBudget> {
        val out = mutableListOf<LegacyScorecardBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyScorecardBudgetStore(): LegacyScorecardBudgetStore {
    return MemoryLegacyScorecardBudgetStore(
        mutableMapOf<String, LegacyScorecardBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyScorecardBudgetStore(
    var inner: LegacyScorecardBudgetStore,
    var cache: TtlCache<LegacyScorecardBudget>,
    var hits: Int,
    var misses: Int,
) : LegacyScorecardBudgetStore {
    override fun find(code: String): LegacyScorecardBudget? {
        val hit = (cache.get(code) as LegacyScorecardBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyScorecardBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyScorecardBudget> = inner.list()
}

fun newCachedLegacyScorecardBudgetStore(inner: LegacyScorecardBudgetStore, ttl: Int, clock: Clock): LegacyScorecardBudgetStore {
    return CachedLegacyScorecardBudgetStore(
        inner = inner,
        cache = newTtlCache<LegacyScorecardBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy scorecard budgets. Empty fields match all. */
data class LegacyScorecardBudgetQuery(
    var country: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyScorecardBudget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<LegacyScorecardBudget>): MutableList<LegacyScorecardBudget> {
        val matched = filter(items) { candidate: LegacyScorecardBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyScorecardBudgets(store: LegacyScorecardBudgetStore, query: LegacyScorecardBudgetQuery): MutableList<LegacyScorecardBudget> {
    return query.apply(store.list())
}
