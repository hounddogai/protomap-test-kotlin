package com.acme.transport.crewing.swap.budget

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap budgets by code. */
interface SwapBudgetStore {
    fun find(code: String): SwapBudget?
    fun save(item: SwapBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapBudgetStore(
    var items: MutableMap<String, SwapBudget>,
    var order: MutableList<String>,
) : SwapBudgetStore {
    override fun find(code: String): SwapBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapBudget) {
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

    override fun list(): MutableList<SwapBudget> {
        val out = mutableListOf<SwapBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapBudgetStore(): SwapBudgetStore {
    return MemorySwapBudgetStore(mutableMapOf<String, SwapBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapBudgetStore(
    var inner: SwapBudgetStore,
    var cache: TtlCache<SwapBudget>,
    var hits: Int,
    var misses: Int,
) : SwapBudgetStore {
    override fun find(code: String): SwapBudget? {
        val hit = (cache.get(code) as SwapBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapBudget> = inner.list()
}

fun newCachedSwapBudgetStore(inner: SwapBudgetStore, ttl: Int, clock: Clock): SwapBudgetStore {
    return CachedSwapBudgetStore(
        inner = inner,
        cache = newTtlCache<SwapBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap budgets. Empty fields match all. */
data class SwapBudgetQuery(
    var depot: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapBudget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapBudget>): MutableList<SwapBudget> {
        val matched = filter(items) { candidate: SwapBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapBudgets(store: SwapBudgetStore, query: SwapBudgetQuery): MutableList<SwapBudget> {
    return query.apply(store.list())
}
