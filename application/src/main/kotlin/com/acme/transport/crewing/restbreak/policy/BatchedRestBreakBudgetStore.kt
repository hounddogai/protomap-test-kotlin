package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched rest break budgets by code. */
interface BatchedRestBreakBudgetStore {
    fun find(code: String): BatchedRestBreakBudget?
    fun save(item: BatchedRestBreakBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRestBreakBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRestBreakBudgetStore(
    var items: MutableMap<String, BatchedRestBreakBudget>,
    var order: MutableList<String>,
) : BatchedRestBreakBudgetStore {
    override fun find(code: String): BatchedRestBreakBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRestBreakBudget) {
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

    override fun list(): MutableList<BatchedRestBreakBudget> {
        val out = mutableListOf<BatchedRestBreakBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRestBreakBudgetStore(): BatchedRestBreakBudgetStore {
    return MemoryBatchedRestBreakBudgetStore(
        mutableMapOf<String, BatchedRestBreakBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRestBreakBudgetStore(
    var inner: BatchedRestBreakBudgetStore,
    var cache: TtlCache<BatchedRestBreakBudget>,
    var hits: Int,
    var misses: Int,
) : BatchedRestBreakBudgetStore {
    override fun find(code: String): BatchedRestBreakBudget? {
        val hit = (cache.get(code) as BatchedRestBreakBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRestBreakBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRestBreakBudget> = inner.list()
}

fun newCachedBatchedRestBreakBudgetStore(inner: BatchedRestBreakBudgetStore, ttl: Int, clock: Clock): BatchedRestBreakBudgetStore {
    return CachedBatchedRestBreakBudgetStore(
        inner = inner,
        cache = newTtlCache<BatchedRestBreakBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched rest break budgets. Empty fields match all. */
data class BatchedRestBreakBudgetQuery(
    var category: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRestBreakBudget): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedRestBreakBudget>): MutableList<BatchedRestBreakBudget> {
        val matched = filter(items) { candidate: BatchedRestBreakBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRestBreakBudgets(store: BatchedRestBreakBudgetStore, query: BatchedRestBreakBudgetQuery): MutableList<BatchedRestBreakBudget> {
    return query.apply(store.list())
}
