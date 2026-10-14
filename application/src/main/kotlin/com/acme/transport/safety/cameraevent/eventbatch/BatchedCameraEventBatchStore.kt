package com.acme.transport.safety.cameraevent.eventbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched camera event batches by code. */
interface BatchedCameraEventBatchStore {
    fun find(code: String): BatchedCameraEventBatch?
    fun save(item: BatchedCameraEventBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCameraEventBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCameraEventBatchStore(
    var items: MutableMap<String, BatchedCameraEventBatch>,
    var order: MutableList<String>,
) : BatchedCameraEventBatchStore {
    override fun find(code: String): BatchedCameraEventBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCameraEventBatch) {
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

    override fun list(): MutableList<BatchedCameraEventBatch> {
        val out = mutableListOf<BatchedCameraEventBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCameraEventBatchStore(): BatchedCameraEventBatchStore {
    return MemoryBatchedCameraEventBatchStore(
        mutableMapOf<String, BatchedCameraEventBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCameraEventBatchStore(
    var inner: BatchedCameraEventBatchStore,
    var cache: TtlCache<BatchedCameraEventBatch>,
    var hits: Int,
    var misses: Int,
) : BatchedCameraEventBatchStore {
    override fun find(code: String): BatchedCameraEventBatch? {
        val hit = (cache.get(code) as BatchedCameraEventBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCameraEventBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCameraEventBatch> = inner.list()
}

fun newCachedBatchedCameraEventBatchStore(inner: BatchedCameraEventBatchStore, ttl: Int, clock: Clock): BatchedCameraEventBatchStore {
    return CachedBatchedCameraEventBatchStore(
        inner = inner,
        cache = newTtlCache<BatchedCameraEventBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched camera event batches. Empty fields match all. */
data class BatchedCameraEventBatchQuery(
    var lane: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCameraEventBatch): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedCameraEventBatch>): MutableList<BatchedCameraEventBatch> {
        val matched = filter(items) { candidate: BatchedCameraEventBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCameraEventBatches(store: BatchedCameraEventBatchStore, query: BatchedCameraEventBatchQuery): MutableList<BatchedCameraEventBatch> {
    return query.apply(store.list())
}
