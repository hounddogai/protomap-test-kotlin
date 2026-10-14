package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched proof of delivery budgets by code. */
interface BatchedProofOfDeliveryBudgetStore {
    fun find(code: String): BatchedProofOfDeliveryBudget?
    fun save(item: BatchedProofOfDeliveryBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedProofOfDeliveryBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedProofOfDeliveryBudgetStore(
    var items: MutableMap<String, BatchedProofOfDeliveryBudget>,
    var order: MutableList<String>,
) : BatchedProofOfDeliveryBudgetStore {
    override fun find(code: String): BatchedProofOfDeliveryBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedProofOfDeliveryBudget) {
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

    override fun list(): MutableList<BatchedProofOfDeliveryBudget> {
        val out = mutableListOf<BatchedProofOfDeliveryBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedProofOfDeliveryBudgetStore(): BatchedProofOfDeliveryBudgetStore {
    return MemoryBatchedProofOfDeliveryBudgetStore(
        mutableMapOf<String, BatchedProofOfDeliveryBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedProofOfDeliveryBudgetStore(
    var inner: BatchedProofOfDeliveryBudgetStore,
    var cache: TtlCache<BatchedProofOfDeliveryBudget>,
    var hits: Int,
    var misses: Int,
) : BatchedProofOfDeliveryBudgetStore {
    override fun find(code: String): BatchedProofOfDeliveryBudget? {
        val hit = (cache.get(code) as BatchedProofOfDeliveryBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedProofOfDeliveryBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedProofOfDeliveryBudget> = inner.list()
}

fun newCachedBatchedProofOfDeliveryBudgetStore(inner: BatchedProofOfDeliveryBudgetStore, ttl: Int, clock: Clock): BatchedProofOfDeliveryBudgetStore {
    return CachedBatchedProofOfDeliveryBudgetStore(
        inner = inner,
        cache = newTtlCache<BatchedProofOfDeliveryBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched proof of delivery budgets. Empty fields match all. */
data class BatchedProofOfDeliveryBudgetQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedProofOfDeliveryBudget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedProofOfDeliveryBudget>): MutableList<BatchedProofOfDeliveryBudget> {
        val matched = filter(items) { candidate: BatchedProofOfDeliveryBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedProofOfDeliveryBudgets(store: BatchedProofOfDeliveryBudgetStore, query: BatchedProofOfDeliveryBudgetQuery): MutableList<BatchedProofOfDeliveryBudget> {
    return query.apply(store.list())
}
