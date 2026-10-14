package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched incident plans by code. */
interface BatchedIncidentPlanStore {
    fun find(code: String): BatchedIncidentPlan?
    fun save(item: BatchedIncidentPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedIncidentPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedIncidentPlanStore(
    var items: MutableMap<String, BatchedIncidentPlan>,
    var order: MutableList<String>,
) : BatchedIncidentPlanStore {
    override fun find(code: String): BatchedIncidentPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedIncidentPlan) {
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

    override fun list(): MutableList<BatchedIncidentPlan> {
        val out = mutableListOf<BatchedIncidentPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedIncidentPlanStore(): BatchedIncidentPlanStore {
    return MemoryBatchedIncidentPlanStore(mutableMapOf<String, BatchedIncidentPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedIncidentPlanStore(
    var inner: BatchedIncidentPlanStore,
    var cache: TtlCache<BatchedIncidentPlan>,
    var hits: Int,
    var misses: Int,
) : BatchedIncidentPlanStore {
    override fun find(code: String): BatchedIncidentPlan? {
        val hit = (cache.get(code) as BatchedIncidentPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedIncidentPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedIncidentPlan> = inner.list()
}

fun newCachedBatchedIncidentPlanStore(inner: BatchedIncidentPlanStore, ttl: Int, clock: Clock): BatchedIncidentPlanStore {
    return CachedBatchedIncidentPlanStore(
        inner = inner,
        cache = newTtlCache<BatchedIncidentPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched incident plans. Empty fields match all. */
data class BatchedIncidentPlanQuery(
    var depot: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedIncidentPlan): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedIncidentPlan>): MutableList<BatchedIncidentPlan> {
        val matched = filter(items) { candidate: BatchedIncidentPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedIncidentPlans(store: BatchedIncidentPlanStore, query: BatchedIncidentPlanQuery): MutableList<BatchedIncidentPlan> {
    return query.apply(store.list())
}
