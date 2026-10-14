package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched cutoff budgets by code. */
interface BatchedCutoffBudgetStore {
    fun find(code: String): BatchedCutoffBudget?
    fun save(item: BatchedCutoffBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCutoffBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCutoffBudgetStore(
    var items: MutableMap<String, BatchedCutoffBudget>,
    var order: MutableList<String>,
) : BatchedCutoffBudgetStore {
    override fun find(code: String): BatchedCutoffBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCutoffBudget) {
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

    override fun list(): MutableList<BatchedCutoffBudget> {
        val out = mutableListOf<BatchedCutoffBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCutoffBudgetStore(): BatchedCutoffBudgetStore {
    return MemoryBatchedCutoffBudgetStore(mutableMapOf<String, BatchedCutoffBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCutoffBudgetStore(
    var inner: BatchedCutoffBudgetStore,
    var cache: TtlCache<BatchedCutoffBudget>,
    var hits: Int,
    var misses: Int,
) : BatchedCutoffBudgetStore {
    override fun find(code: String): BatchedCutoffBudget? {
        val hit = (cache.get(code) as BatchedCutoffBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCutoffBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCutoffBudget> = inner.list()
}

fun newCachedBatchedCutoffBudgetStore(inner: BatchedCutoffBudgetStore, ttl: Int, clock: Clock): BatchedCutoffBudgetStore {
    return CachedBatchedCutoffBudgetStore(
        inner = inner,
        cache = newTtlCache<BatchedCutoffBudget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched cutoff budgets. Empty fields match all. */
data class BatchedCutoffBudgetQuery(
    var locale: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCutoffBudget): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedCutoffBudget>): MutableList<BatchedCutoffBudget> {
        val matched = filter(items) { candidate: BatchedCutoffBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCutoffBudgets(store: BatchedCutoffBudgetStore, query: BatchedCutoffBudgetQuery): MutableList<BatchedCutoffBudget> {
    return query.apply(store.list())
}
