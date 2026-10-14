package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched drop budgets by code. */
interface BatchedDropBudgetStore {
    fun find(code: String): BatchedDropBudget?
    fun save(item: BatchedDropBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDropBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDropBudgetStore(
    var items: MutableMap<String, BatchedDropBudget>,
    var order: MutableList<String>,
) : BatchedDropBudgetStore {
    override fun find(code: String): BatchedDropBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDropBudget) {
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

    override fun list(): MutableList<BatchedDropBudget> {
        val out = mutableListOf<BatchedDropBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDropBudgetStore(): BatchedDropBudgetStore {
    return MemoryBatchedDropBudgetStore(mutableMapOf<String, BatchedDropBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDropBudgetStore(
    var inner: BatchedDropBudgetStore,
    var cache: TtlCache<BatchedDropBudget>,
    var hits: Int,
    var misses: Int,
) : BatchedDropBudgetStore {
    override fun find(code: String): BatchedDropBudget? {
        val hit = (cache.get(code) as BatchedDropBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDropBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDropBudget> = inner.list()
}

fun newCachedBatchedDropBudgetStore(inner: BatchedDropBudgetStore, ttl: Int, clock: Clock): BatchedDropBudgetStore {
    return CachedBatchedDropBudgetStore(
        inner = inner,
        cache = newTtlCache<BatchedDropBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched drop budgets. Empty fields match all. */
data class BatchedDropBudgetQuery(
    var unit: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDropBudget): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDropBudget>): MutableList<BatchedDropBudget> {
        val matched = filter(items) { candidate: BatchedDropBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDropBudgets(store: BatchedDropBudgetStore, query: BatchedDropBudgetQuery): MutableList<BatchedDropBudget> {
    return query.apply(store.list())
}
