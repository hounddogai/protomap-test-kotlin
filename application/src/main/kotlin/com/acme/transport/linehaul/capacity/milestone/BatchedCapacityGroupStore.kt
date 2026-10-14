package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched capacity groups by code. */
interface BatchedCapacityGroupStore {
    fun find(code: String): BatchedCapacityGroup?
    fun save(item: BatchedCapacityGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCapacityGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCapacityGroupStore(
    var items: MutableMap<String, BatchedCapacityGroup>,
    var order: MutableList<String>,
) : BatchedCapacityGroupStore {
    override fun find(code: String): BatchedCapacityGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCapacityGroup) {
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

    override fun list(): MutableList<BatchedCapacityGroup> {
        val out = mutableListOf<BatchedCapacityGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCapacityGroupStore(): BatchedCapacityGroupStore {
    return MemoryBatchedCapacityGroupStore(
        mutableMapOf<String, BatchedCapacityGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCapacityGroupStore(
    var inner: BatchedCapacityGroupStore,
    var cache: TtlCache<BatchedCapacityGroup>,
    var hits: Int,
    var misses: Int,
) : BatchedCapacityGroupStore {
    override fun find(code: String): BatchedCapacityGroup? {
        val hit = (cache.get(code) as BatchedCapacityGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCapacityGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCapacityGroup> = inner.list()
}

fun newCachedBatchedCapacityGroupStore(inner: BatchedCapacityGroupStore, ttl: Int, clock: Clock): BatchedCapacityGroupStore {
    return CachedBatchedCapacityGroupStore(
        inner = inner,
        cache = newTtlCache<BatchedCapacityGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched capacity groups. Empty fields match all. */
data class BatchedCapacityGroupQuery(
    var unit: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCapacityGroup): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedCapacityGroup>): MutableList<BatchedCapacityGroup> {
        val matched = filter(items) { candidate: BatchedCapacityGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCapacityGroups(store: BatchedCapacityGroupStore, query: BatchedCapacityGroupQuery): MutableList<BatchedCapacityGroup> {
    return query.apply(store.list())
}
