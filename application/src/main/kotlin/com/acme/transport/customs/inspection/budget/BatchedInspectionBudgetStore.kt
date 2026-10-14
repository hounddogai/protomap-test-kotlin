package com.acme.transport.customs.inspection.budget

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched inspection budgets by code. */
interface BatchedInspectionBudgetStore {
    fun find(code: String): BatchedInspectionBudget?
    fun save(item: BatchedInspectionBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedInspectionBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedInspectionBudgetStore(
    var items: MutableMap<String, BatchedInspectionBudget>,
    var order: MutableList<String>,
) : BatchedInspectionBudgetStore {
    override fun find(code: String): BatchedInspectionBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedInspectionBudget) {
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

    override fun list(): MutableList<BatchedInspectionBudget> {
        val out = mutableListOf<BatchedInspectionBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedInspectionBudgetStore(): BatchedInspectionBudgetStore {
    return MemoryBatchedInspectionBudgetStore(
        mutableMapOf<String, BatchedInspectionBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedInspectionBudgetStore(
    var inner: BatchedInspectionBudgetStore,
    var cache: TtlCache<BatchedInspectionBudget>,
    var hits: Int,
    var misses: Int,
) : BatchedInspectionBudgetStore {
    override fun find(code: String): BatchedInspectionBudget? {
        val hit = (cache.get(code) as BatchedInspectionBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedInspectionBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedInspectionBudget> = inner.list()
}

fun newCachedBatchedInspectionBudgetStore(inner: BatchedInspectionBudgetStore, ttl: Int, clock: Clock): BatchedInspectionBudgetStore {
    return CachedBatchedInspectionBudgetStore(
        inner = inner,
        cache = newTtlCache<BatchedInspectionBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched inspection budgets. Empty fields match all. */
data class BatchedInspectionBudgetQuery(
    var unit: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedInspectionBudget): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<BatchedInspectionBudget>): MutableList<BatchedInspectionBudget> {
        val matched = filter(items) { candidate: BatchedInspectionBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedInspectionBudgets(store: BatchedInspectionBudgetStore, query: BatchedInspectionBudgetQuery): MutableList<BatchedInspectionBudget> {
    return query.apply(store.list())
}
