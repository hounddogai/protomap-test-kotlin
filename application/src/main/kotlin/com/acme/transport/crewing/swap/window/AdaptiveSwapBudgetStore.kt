package com.acme.transport.crewing.swap.window

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive swap budgets by code. */
interface AdaptiveSwapBudgetStore {
    fun find(code: String): AdaptiveSwapBudget?
    fun save(item: AdaptiveSwapBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveSwapBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveSwapBudgetStore(
    var items: MutableMap<String, AdaptiveSwapBudget>,
    var order: MutableList<String>,
) : AdaptiveSwapBudgetStore {
    override fun find(code: String): AdaptiveSwapBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveSwapBudget) {
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

    override fun list(): MutableList<AdaptiveSwapBudget> {
        val out = mutableListOf<AdaptiveSwapBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveSwapBudgetStore(): AdaptiveSwapBudgetStore {
    return MemoryAdaptiveSwapBudgetStore(mutableMapOf<String, AdaptiveSwapBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveSwapBudgetStore(
    var inner: AdaptiveSwapBudgetStore,
    var cache: TtlCache<AdaptiveSwapBudget>,
    var hits: Int,
    var misses: Int,
) : AdaptiveSwapBudgetStore {
    override fun find(code: String): AdaptiveSwapBudget? {
        val hit = (cache.get(code) as AdaptiveSwapBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveSwapBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveSwapBudget> = inner.list()
}

fun newCachedAdaptiveSwapBudgetStore(inner: AdaptiveSwapBudgetStore, ttl: Int, clock: Clock): AdaptiveSwapBudgetStore {
    return CachedAdaptiveSwapBudgetStore(
        inner = inner,
        cache = newTtlCache<AdaptiveSwapBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive swap budgets. Empty fields match all. */
data class AdaptiveSwapBudgetQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveSwapBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveSwapBudget>): MutableList<AdaptiveSwapBudget> {
        val matched = filter(items) { candidate: AdaptiveSwapBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveSwapBudgets(store: AdaptiveSwapBudgetStore, query: AdaptiveSwapBudgetQuery): MutableList<AdaptiveSwapBudget> {
    return query.apply(store.list())
}
