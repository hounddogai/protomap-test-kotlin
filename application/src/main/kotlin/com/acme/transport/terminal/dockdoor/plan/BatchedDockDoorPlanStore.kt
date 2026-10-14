package com.acme.transport.terminal.dockdoor.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched dock door plans by code. */
interface BatchedDockDoorPlanStore {
    fun find(code: String): BatchedDockDoorPlan?
    fun save(item: BatchedDockDoorPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDockDoorPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDockDoorPlanStore(
    var items: MutableMap<String, BatchedDockDoorPlan>,
    var order: MutableList<String>,
) : BatchedDockDoorPlanStore {
    override fun find(code: String): BatchedDockDoorPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDockDoorPlan) {
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

    override fun list(): MutableList<BatchedDockDoorPlan> {
        val out = mutableListOf<BatchedDockDoorPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDockDoorPlanStore(): BatchedDockDoorPlanStore {
    return MemoryBatchedDockDoorPlanStore(mutableMapOf<String, BatchedDockDoorPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDockDoorPlanStore(
    var inner: BatchedDockDoorPlanStore,
    var cache: TtlCache<BatchedDockDoorPlan>,
    var hits: Int,
    var misses: Int,
) : BatchedDockDoorPlanStore {
    override fun find(code: String): BatchedDockDoorPlan? {
        val hit = (cache.get(code) as BatchedDockDoorPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDockDoorPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDockDoorPlan> = inner.list()
}

fun newCachedBatchedDockDoorPlanStore(inner: BatchedDockDoorPlanStore, ttl: Int, clock: Clock): BatchedDockDoorPlanStore {
    return CachedBatchedDockDoorPlanStore(
        inner = inner,
        cache = newTtlCache<BatchedDockDoorPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched dock door plans. Empty fields match all. */
data class BatchedDockDoorPlanQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDockDoorPlan): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<BatchedDockDoorPlan>): MutableList<BatchedDockDoorPlan> {
        val matched = filter(items) { candidate: BatchedDockDoorPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDockDoorPlans(store: BatchedDockDoorPlanStore, query: BatchedDockDoorPlanQuery): MutableList<BatchedDockDoorPlan> {
    return query.apply(store.list())
}
