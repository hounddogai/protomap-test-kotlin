package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched trailer yard budgets by code. */
interface BatchedTrailerYardBudgetStore {
    fun find(code: String): BatchedTrailerYardBudget?
    fun save(item: BatchedTrailerYardBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTrailerYardBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTrailerYardBudgetStore(
    var items: MutableMap<String, BatchedTrailerYardBudget>,
    var order: MutableList<String>,
) : BatchedTrailerYardBudgetStore {
    override fun find(code: String): BatchedTrailerYardBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTrailerYardBudget) {
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

    override fun list(): MutableList<BatchedTrailerYardBudget> {
        val out = mutableListOf<BatchedTrailerYardBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTrailerYardBudgetStore(): BatchedTrailerYardBudgetStore {
    return MemoryBatchedTrailerYardBudgetStore(
        mutableMapOf<String, BatchedTrailerYardBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTrailerYardBudgetStore(
    var inner: BatchedTrailerYardBudgetStore,
    var cache: TtlCache<BatchedTrailerYardBudget>,
    var hits: Int,
    var misses: Int,
) : BatchedTrailerYardBudgetStore {
    override fun find(code: String): BatchedTrailerYardBudget? {
        val hit = (cache.get(code) as BatchedTrailerYardBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTrailerYardBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTrailerYardBudget> = inner.list()
}

fun newCachedBatchedTrailerYardBudgetStore(inner: BatchedTrailerYardBudgetStore, ttl: Int, clock: Clock): BatchedTrailerYardBudgetStore {
    return CachedBatchedTrailerYardBudgetStore(
        inner = inner,
        cache = newTtlCache<BatchedTrailerYardBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched trailer yard budgets. Empty fields match all. */
data class BatchedTrailerYardBudgetQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTrailerYardBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<BatchedTrailerYardBudget>): MutableList<BatchedTrailerYardBudget> {
        val matched = filter(items) { candidate: BatchedTrailerYardBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTrailerYardBudgets(store: BatchedTrailerYardBudgetStore, query: BatchedTrailerYardBudgetQuery): MutableList<BatchedTrailerYardBudget> {
    return query.apply(store.list())
}
