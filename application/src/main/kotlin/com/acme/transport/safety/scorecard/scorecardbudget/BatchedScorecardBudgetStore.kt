package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched scorecard budgets by code. */
interface BatchedScorecardBudgetStore {
    fun find(code: String): BatchedScorecardBudget?
    fun save(item: BatchedScorecardBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedScorecardBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedScorecardBudgetStore(
    var items: MutableMap<String, BatchedScorecardBudget>,
    var order: MutableList<String>,
) : BatchedScorecardBudgetStore {
    override fun find(code: String): BatchedScorecardBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedScorecardBudget) {
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

    override fun list(): MutableList<BatchedScorecardBudget> {
        val out = mutableListOf<BatchedScorecardBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedScorecardBudgetStore(): BatchedScorecardBudgetStore {
    return MemoryBatchedScorecardBudgetStore(
        mutableMapOf<String, BatchedScorecardBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedScorecardBudgetStore(
    var inner: BatchedScorecardBudgetStore,
    var cache: TtlCache<BatchedScorecardBudget>,
    var hits: Int,
    var misses: Int,
) : BatchedScorecardBudgetStore {
    override fun find(code: String): BatchedScorecardBudget? {
        val hit = (cache.get(code) as BatchedScorecardBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedScorecardBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedScorecardBudget> = inner.list()
}

fun newCachedBatchedScorecardBudgetStore(inner: BatchedScorecardBudgetStore, ttl: Int, clock: Clock): BatchedScorecardBudgetStore {
    return CachedBatchedScorecardBudgetStore(
        inner = inner,
        cache = newTtlCache<BatchedScorecardBudget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched scorecard budgets. Empty fields match all. */
data class BatchedScorecardBudgetQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedScorecardBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedScorecardBudget>): MutableList<BatchedScorecardBudget> {
        val matched = filter(items) { candidate: BatchedScorecardBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedScorecardBudgets(store: BatchedScorecardBudgetStore, query: BatchedScorecardBudgetQuery): MutableList<BatchedScorecardBudget> {
    return query.apply(store.list())
}
