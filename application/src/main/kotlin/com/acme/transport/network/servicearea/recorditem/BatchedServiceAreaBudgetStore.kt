package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched service area budgets by code. */
interface BatchedServiceAreaBudgetStore {
    fun find(code: String): BatchedServiceAreaBudget?
    fun save(item: BatchedServiceAreaBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedServiceAreaBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedServiceAreaBudgetStore(
    var items: MutableMap<String, BatchedServiceAreaBudget>,
    var order: MutableList<String>,
) : BatchedServiceAreaBudgetStore {
    override fun find(code: String): BatchedServiceAreaBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedServiceAreaBudget) {
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

    override fun list(): MutableList<BatchedServiceAreaBudget> {
        val out = mutableListOf<BatchedServiceAreaBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedServiceAreaBudgetStore(): BatchedServiceAreaBudgetStore {
    return MemoryBatchedServiceAreaBudgetStore(
        mutableMapOf<String, BatchedServiceAreaBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedServiceAreaBudgetStore(
    var inner: BatchedServiceAreaBudgetStore,
    var cache: TtlCache<BatchedServiceAreaBudget>,
    var hits: Int,
    var misses: Int,
) : BatchedServiceAreaBudgetStore {
    override fun find(code: String): BatchedServiceAreaBudget? {
        val hit = (cache.get(code) as BatchedServiceAreaBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedServiceAreaBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedServiceAreaBudget> = inner.list()
}

fun newCachedBatchedServiceAreaBudgetStore(inner: BatchedServiceAreaBudgetStore, ttl: Int, clock: Clock): BatchedServiceAreaBudgetStore {
    return CachedBatchedServiceAreaBudgetStore(
        inner = inner,
        cache = newTtlCache<BatchedServiceAreaBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched service area budgets. Empty fields match all. */
data class BatchedServiceAreaBudgetQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedServiceAreaBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedServiceAreaBudget>): MutableList<BatchedServiceAreaBudget> {
        val matched = filter(items) { candidate: BatchedServiceAreaBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedServiceAreaBudgets(store: BatchedServiceAreaBudgetStore, query: BatchedServiceAreaBudgetQuery): MutableList<BatchedServiceAreaBudget> {
    return query.apply(store.list())
}
