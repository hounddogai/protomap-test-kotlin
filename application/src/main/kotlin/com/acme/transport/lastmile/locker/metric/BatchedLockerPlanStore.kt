package com.acme.transport.lastmile.locker.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched locker plans by code. */
interface BatchedLockerPlanStore {
    fun find(code: String): BatchedLockerPlan?
    fun save(item: BatchedLockerPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedLockerPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedLockerPlanStore(
    var items: MutableMap<String, BatchedLockerPlan>,
    var order: MutableList<String>,
) : BatchedLockerPlanStore {
    override fun find(code: String): BatchedLockerPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedLockerPlan) {
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

    override fun list(): MutableList<BatchedLockerPlan> {
        val out = mutableListOf<BatchedLockerPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedLockerPlanStore(): BatchedLockerPlanStore {
    return MemoryBatchedLockerPlanStore(mutableMapOf<String, BatchedLockerPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedLockerPlanStore(
    var inner: BatchedLockerPlanStore,
    var cache: TtlCache<BatchedLockerPlan>,
    var hits: Int,
    var misses: Int,
) : BatchedLockerPlanStore {
    override fun find(code: String): BatchedLockerPlan? {
        val hit = (cache.get(code) as BatchedLockerPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedLockerPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedLockerPlan> = inner.list()
}

fun newCachedBatchedLockerPlanStore(inner: BatchedLockerPlanStore, ttl: Int, clock: Clock): BatchedLockerPlanStore {
    return CachedBatchedLockerPlanStore(
        inner = inner,
        cache = newTtlCache<BatchedLockerPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched locker plans. Empty fields match all. */
data class BatchedLockerPlanQuery(
    var lane: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedLockerPlan): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<BatchedLockerPlan>): MutableList<BatchedLockerPlan> {
        val matched = filter(items) { candidate: BatchedLockerPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedLockerPlans(store: BatchedLockerPlanStore, query: BatchedLockerPlanQuery): MutableList<BatchedLockerPlan> {
    return query.apply(store.list())
}
