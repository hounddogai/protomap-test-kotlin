package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps seasonal coaching budgets by code. */
interface SeasonalCoachingBudgetStore {
    fun find(code: String): SeasonalCoachingBudget?
    fun save(item: SeasonalCoachingBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCoachingBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCoachingBudgetStore(
    var items: MutableMap<String, SeasonalCoachingBudget>,
    var order: MutableList<String>,
) : SeasonalCoachingBudgetStore {
    override fun find(code: String): SeasonalCoachingBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCoachingBudget) {
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

    override fun list(): MutableList<SeasonalCoachingBudget> {
        val out = mutableListOf<SeasonalCoachingBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCoachingBudgetStore(): SeasonalCoachingBudgetStore {
    return MemorySeasonalCoachingBudgetStore(
        mutableMapOf<String, SeasonalCoachingBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCoachingBudgetStore(
    var inner: SeasonalCoachingBudgetStore,
    var cache: TtlCache<SeasonalCoachingBudget>,
    var hits: Int,
    var misses: Int,
) : SeasonalCoachingBudgetStore {
    override fun find(code: String): SeasonalCoachingBudget? {
        val hit = (cache.get(code) as SeasonalCoachingBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCoachingBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCoachingBudget> = inner.list()
}

fun newCachedSeasonalCoachingBudgetStore(inner: SeasonalCoachingBudgetStore, ttl: Int, clock: Clock): SeasonalCoachingBudgetStore {
    return CachedSeasonalCoachingBudgetStore(
        inner = inner,
        cache = newTtlCache<SeasonalCoachingBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal coaching budgets. Empty fields match all. */
data class SeasonalCoachingBudgetQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCoachingBudget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalCoachingBudget>): MutableList<SeasonalCoachingBudget> {
        val matched = filter(items) { candidate: SeasonalCoachingBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCoachingBudgets(store: SeasonalCoachingBudgetStore, query: SeasonalCoachingBudgetQuery): MutableList<SeasonalCoachingBudget> {
    return query.apply(store.list())
}
