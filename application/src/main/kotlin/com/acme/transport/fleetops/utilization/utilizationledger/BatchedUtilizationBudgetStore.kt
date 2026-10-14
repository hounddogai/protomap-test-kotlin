package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched utilization budgets by code. */
interface BatchedUtilizationBudgetStore {
    fun find(code: String): BatchedUtilizationBudget?
    fun save(item: BatchedUtilizationBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedUtilizationBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedUtilizationBudgetStore(
    var items: MutableMap<String, BatchedUtilizationBudget>,
    var order: MutableList<String>,
) : BatchedUtilizationBudgetStore {
    override fun find(code: String): BatchedUtilizationBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedUtilizationBudget) {
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

    override fun list(): MutableList<BatchedUtilizationBudget> {
        val out = mutableListOf<BatchedUtilizationBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedUtilizationBudgetStore(): BatchedUtilizationBudgetStore {
    return MemoryBatchedUtilizationBudgetStore(
        mutableMapOf<String, BatchedUtilizationBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedUtilizationBudgetStore(
    var inner: BatchedUtilizationBudgetStore,
    var cache: TtlCache<BatchedUtilizationBudget>,
    var hits: Int,
    var misses: Int,
) : BatchedUtilizationBudgetStore {
    override fun find(code: String): BatchedUtilizationBudget? {
        val hit = (cache.get(code) as BatchedUtilizationBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedUtilizationBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedUtilizationBudget> = inner.list()
}

fun newCachedBatchedUtilizationBudgetStore(inner: BatchedUtilizationBudgetStore, ttl: Int, clock: Clock): BatchedUtilizationBudgetStore {
    return CachedBatchedUtilizationBudgetStore(
        inner = inner,
        cache = newTtlCache<BatchedUtilizationBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched utilization budgets. Empty fields match all. */
data class BatchedUtilizationBudgetQuery(
    var channel: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedUtilizationBudget): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedUtilizationBudget>): MutableList<BatchedUtilizationBudget> {
        val matched = filter(items) { candidate: BatchedUtilizationBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedUtilizationBudgets(store: BatchedUtilizationBudgetStore, query: BatchedUtilizationBudgetQuery): MutableList<BatchedUtilizationBudget> {
    return query.apply(store.list())
}
