package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break budgets by code. */
interface RestBreakBudgetStore {
    fun find(code: String): RestBreakBudget?
    fun save(item: RestBreakBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakBudgetStore(
    var items: MutableMap<String, RestBreakBudget>,
    var order: MutableList<String>,
) : RestBreakBudgetStore {
    override fun find(code: String): RestBreakBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakBudget) {
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

    override fun list(): MutableList<RestBreakBudget> {
        val out = mutableListOf<RestBreakBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakBudgetStore(): RestBreakBudgetStore {
    return MemoryRestBreakBudgetStore(mutableMapOf<String, RestBreakBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakBudgetStore(
    var inner: RestBreakBudgetStore,
    var cache: TtlCache<RestBreakBudget>,
    var hits: Int,
    var misses: Int,
) : RestBreakBudgetStore {
    override fun find(code: String): RestBreakBudget? {
        val hit = (cache.get(code) as RestBreakBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakBudget> = inner.list()
}

fun newCachedRestBreakBudgetStore(inner: RestBreakBudgetStore, ttl: Int, clock: Clock): RestBreakBudgetStore {
    return CachedRestBreakBudgetStore(
        inner = inner,
        cache = newTtlCache<RestBreakBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break budgets. Empty fields match all. */
data class RestBreakBudgetQuery(
    var category: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakBudget): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakBudget>): MutableList<RestBreakBudget> {
        val matched = filter(items) { candidate: RestBreakBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakBudgets(store: RestBreakBudgetStore, query: RestBreakBudgetQuery): MutableList<RestBreakBudget> {
    return query.apply(store.list())
}
