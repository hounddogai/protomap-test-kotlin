package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched capacity batches by code. */
interface BatchedCapacityBatchStore {
    fun find(code: String): BatchedCapacityBatch?
    fun save(item: BatchedCapacityBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCapacityBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCapacityBatchStore(
    var items: MutableMap<String, BatchedCapacityBatch>,
    var order: MutableList<String>,
) : BatchedCapacityBatchStore {
    override fun find(code: String): BatchedCapacityBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCapacityBatch) {
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

    override fun list(): MutableList<BatchedCapacityBatch> {
        val out = mutableListOf<BatchedCapacityBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCapacityBatchStore(): BatchedCapacityBatchStore {
    return MemoryBatchedCapacityBatchStore(
        mutableMapOf<String, BatchedCapacityBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCapacityBatchStore(
    var inner: BatchedCapacityBatchStore,
    var cache: TtlCache<BatchedCapacityBatch>,
    var hits: Int,
    var misses: Int,
) : BatchedCapacityBatchStore {
    override fun find(code: String): BatchedCapacityBatch? {
        val hit = (cache.get(code) as BatchedCapacityBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCapacityBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCapacityBatch> = inner.list()
}

fun newCachedBatchedCapacityBatchStore(inner: BatchedCapacityBatchStore, ttl: Int, clock: Clock): BatchedCapacityBatchStore {
    return CachedBatchedCapacityBatchStore(
        inner = inner,
        cache = newTtlCache<BatchedCapacityBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched capacity batches. Empty fields match all. */
data class BatchedCapacityBatchQuery(
    var region: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCapacityBatch): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedCapacityBatch>): MutableList<BatchedCapacityBatch> {
        val matched = filter(items) { candidate: BatchedCapacityBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCapacityBatches(store: BatchedCapacityBatchStore, query: BatchedCapacityBatchQuery): MutableList<BatchedCapacityBatch> {
    return query.apply(store.list())
}
