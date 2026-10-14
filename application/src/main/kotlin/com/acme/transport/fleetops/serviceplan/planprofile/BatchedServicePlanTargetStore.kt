package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched service plan targets by code. */
interface BatchedServicePlanTargetStore {
    fun find(code: String): BatchedServicePlanTarget?
    fun save(item: BatchedServicePlanTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedServicePlanTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedServicePlanTargetStore(
    var items: MutableMap<String, BatchedServicePlanTarget>,
    var order: MutableList<String>,
) : BatchedServicePlanTargetStore {
    override fun find(code: String): BatchedServicePlanTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedServicePlanTarget) {
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

    override fun list(): MutableList<BatchedServicePlanTarget> {
        val out = mutableListOf<BatchedServicePlanTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedServicePlanTargetStore(): BatchedServicePlanTargetStore {
    return MemoryBatchedServicePlanTargetStore(
        mutableMapOf<String, BatchedServicePlanTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedServicePlanTargetStore(
    var inner: BatchedServicePlanTargetStore,
    var cache: TtlCache<BatchedServicePlanTarget>,
    var hits: Int,
    var misses: Int,
) : BatchedServicePlanTargetStore {
    override fun find(code: String): BatchedServicePlanTarget? {
        val hit = (cache.get(code) as BatchedServicePlanTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedServicePlanTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedServicePlanTarget> = inner.list()
}

fun newCachedBatchedServicePlanTargetStore(inner: BatchedServicePlanTargetStore, ttl: Int, clock: Clock): BatchedServicePlanTargetStore {
    return CachedBatchedServicePlanTargetStore(
        inner = inner,
        cache = newTtlCache<BatchedServicePlanTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched service plan targets. Empty fields match all. */
data class BatchedServicePlanTargetQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedServicePlanTarget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedServicePlanTarget>): MutableList<BatchedServicePlanTarget> {
        val matched = filter(items) { candidate: BatchedServicePlanTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedServicePlanTargets(store: BatchedServicePlanTargetStore, query: BatchedServicePlanTargetQuery): MutableList<BatchedServicePlanTarget> {
    return query.apply(store.list())
}
