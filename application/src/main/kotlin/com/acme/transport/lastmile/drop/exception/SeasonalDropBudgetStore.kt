package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal drop budgets by code. */
interface SeasonalDropBudgetStore {
    fun find(code: String): SeasonalDropBudget?
    fun save(item: SeasonalDropBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDropBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDropBudgetStore(
    var items: MutableMap<String, SeasonalDropBudget>,
    var order: MutableList<String>,
) : SeasonalDropBudgetStore {
    override fun find(code: String): SeasonalDropBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDropBudget) {
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

    override fun list(): MutableList<SeasonalDropBudget> {
        val out = mutableListOf<SeasonalDropBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDropBudgetStore(): SeasonalDropBudgetStore {
    return MemorySeasonalDropBudgetStore(mutableMapOf<String, SeasonalDropBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDropBudgetStore(
    var inner: SeasonalDropBudgetStore,
    var cache: TtlCache<SeasonalDropBudget>,
    var hits: Int,
    var misses: Int,
) : SeasonalDropBudgetStore {
    override fun find(code: String): SeasonalDropBudget? {
        val hit = (cache.get(code) as SeasonalDropBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDropBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDropBudget> = inner.list()
}

fun newCachedSeasonalDropBudgetStore(inner: SeasonalDropBudgetStore, ttl: Int, clock: Clock): SeasonalDropBudgetStore {
    return CachedSeasonalDropBudgetStore(
        inner = inner,
        cache = newTtlCache<SeasonalDropBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal drop budgets. Empty fields match all. */
data class SeasonalDropBudgetQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDropBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<SeasonalDropBudget>): MutableList<SeasonalDropBudget> {
        val matched = filter(items) { candidate: SeasonalDropBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDropBudgets(store: SeasonalDropBudgetStore, query: SeasonalDropBudgetQuery): MutableList<SeasonalDropBudget> {
    return query.apply(store.list())
}
