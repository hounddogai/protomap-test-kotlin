package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched scorecard plans by code. */
interface BatchedScorecardPlanStore {
    fun find(code: String): BatchedScorecardPlan?
    fun save(item: BatchedScorecardPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedScorecardPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedScorecardPlanStore(
    var items: MutableMap<String, BatchedScorecardPlan>,
    var order: MutableList<String>,
) : BatchedScorecardPlanStore {
    override fun find(code: String): BatchedScorecardPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedScorecardPlan) {
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

    override fun list(): MutableList<BatchedScorecardPlan> {
        val out = mutableListOf<BatchedScorecardPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedScorecardPlanStore(): BatchedScorecardPlanStore {
    return MemoryBatchedScorecardPlanStore(
        mutableMapOf<String, BatchedScorecardPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedScorecardPlanStore(
    var inner: BatchedScorecardPlanStore,
    var cache: TtlCache<BatchedScorecardPlan>,
    var hits: Int,
    var misses: Int,
) : BatchedScorecardPlanStore {
    override fun find(code: String): BatchedScorecardPlan? {
        val hit = (cache.get(code) as BatchedScorecardPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedScorecardPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedScorecardPlan> = inner.list()
}

fun newCachedBatchedScorecardPlanStore(inner: BatchedScorecardPlanStore, ttl: Int, clock: Clock): BatchedScorecardPlanStore {
    return CachedBatchedScorecardPlanStore(
        inner = inner,
        cache = newTtlCache<BatchedScorecardPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched scorecard plans. Empty fields match all. */
data class BatchedScorecardPlanQuery(
    var category: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedScorecardPlan): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedScorecardPlan>): MutableList<BatchedScorecardPlan> {
        val matched = filter(items) { candidate: BatchedScorecardPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedScorecardPlans(store: BatchedScorecardPlanStore, query: BatchedScorecardPlanQuery): MutableList<BatchedScorecardPlan> {
    return query.apply(store.list())
}
