package com.acme.transport.safety.scorecard.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard budgets by code. */
interface ScorecardBudgetStore {
    fun find(code: String): ScorecardBudget?
    fun save(item: ScorecardBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardBudgetStore(
    var items: MutableMap<String, ScorecardBudget>,
    var order: MutableList<String>,
) : ScorecardBudgetStore {
    override fun find(code: String): ScorecardBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardBudget) {
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

    override fun list(): MutableList<ScorecardBudget> {
        val out = mutableListOf<ScorecardBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardBudgetStore(): ScorecardBudgetStore {
    return MemoryScorecardBudgetStore(mutableMapOf<String, ScorecardBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardBudgetStore(
    var inner: ScorecardBudgetStore,
    var cache: TtlCache<ScorecardBudget>,
    var hits: Int,
    var misses: Int,
) : ScorecardBudgetStore {
    override fun find(code: String): ScorecardBudget? {
        val hit = (cache.get(code) as ScorecardBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardBudget> = inner.list()
}

fun newCachedScorecardBudgetStore(inner: ScorecardBudgetStore, ttl: Int, clock: Clock): ScorecardBudgetStore {
    return CachedScorecardBudgetStore(
        inner = inner,
        cache = newTtlCache<ScorecardBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard budgets. Empty fields match all. */
data class ScorecardBudgetQuery(
    var segment: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardBudget): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardBudget>): MutableList<ScorecardBudget> {
        val matched = filter(items) { candidate: ScorecardBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardBudgets(store: ScorecardBudgetStore, query: ScorecardBudgetQuery): MutableList<ScorecardBudget> {
    return query.apply(store.list())
}
