package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched service plan summaries by code. */
interface BatchedServicePlanSummaryStore {
    fun find(code: String): BatchedServicePlanSummary?
    fun save(item: BatchedServicePlanSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedServicePlanSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedServicePlanSummaryStore(
    var items: MutableMap<String, BatchedServicePlanSummary>,
    var order: MutableList<String>,
) : BatchedServicePlanSummaryStore {
    override fun find(code: String): BatchedServicePlanSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedServicePlanSummary) {
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

    override fun list(): MutableList<BatchedServicePlanSummary> {
        val out = mutableListOf<BatchedServicePlanSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedServicePlanSummaryStore(): BatchedServicePlanSummaryStore {
    return MemoryBatchedServicePlanSummaryStore(
        mutableMapOf<String, BatchedServicePlanSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedServicePlanSummaryStore(
    var inner: BatchedServicePlanSummaryStore,
    var cache: TtlCache<BatchedServicePlanSummary>,
    var hits: Int,
    var misses: Int,
) : BatchedServicePlanSummaryStore {
    override fun find(code: String): BatchedServicePlanSummary? {
        val hit = (cache.get(code) as BatchedServicePlanSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedServicePlanSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedServicePlanSummary> = inner.list()
}

fun newCachedBatchedServicePlanSummaryStore(inner: BatchedServicePlanSummaryStore, ttl: Int, clock: Clock): BatchedServicePlanSummaryStore {
    return CachedBatchedServicePlanSummaryStore(
        inner = inner,
        cache = newTtlCache<BatchedServicePlanSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched service plan summaries. Empty fields match all. */
data class BatchedServicePlanSummaryQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedServicePlanSummary): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedServicePlanSummary>): MutableList<BatchedServicePlanSummary> {
        val matched = filter(items) { candidate: BatchedServicePlanSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedServicePlanSummaries(store: BatchedServicePlanSummaryStore, query: BatchedServicePlanSummaryQuery): MutableList<BatchedServicePlanSummary> {
    return query.apply(store.list())
}
