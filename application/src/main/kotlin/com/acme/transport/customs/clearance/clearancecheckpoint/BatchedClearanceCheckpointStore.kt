package com.acme.transport.customs.clearance.clearancecheckpoint

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched clearance checkpoints by code. */
interface BatchedClearanceCheckpointStore {
    fun find(code: String): BatchedClearanceCheckpoint?
    fun save(item: BatchedClearanceCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedClearanceCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedClearanceCheckpointStore(
    var items: MutableMap<String, BatchedClearanceCheckpoint>,
    var order: MutableList<String>,
) : BatchedClearanceCheckpointStore {
    override fun find(code: String): BatchedClearanceCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedClearanceCheckpoint) {
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

    override fun list(): MutableList<BatchedClearanceCheckpoint> {
        val out = mutableListOf<BatchedClearanceCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedClearanceCheckpointStore(): BatchedClearanceCheckpointStore {
    return MemoryBatchedClearanceCheckpointStore(
        mutableMapOf<String, BatchedClearanceCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedClearanceCheckpointStore(
    var inner: BatchedClearanceCheckpointStore,
    var cache: TtlCache<BatchedClearanceCheckpoint>,
    var hits: Int,
    var misses: Int,
) : BatchedClearanceCheckpointStore {
    override fun find(code: String): BatchedClearanceCheckpoint? {
        val hit = (cache.get(code) as BatchedClearanceCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedClearanceCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedClearanceCheckpoint> = inner.list()
}

fun newCachedBatchedClearanceCheckpointStore(inner: BatchedClearanceCheckpointStore, ttl: Int, clock: Clock): BatchedClearanceCheckpointStore {
    return CachedBatchedClearanceCheckpointStore(
        inner = inner,
        cache = newTtlCache<BatchedClearanceCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched clearance checkpoints. Empty fields match all. */
data class BatchedClearanceCheckpointQuery(
    var depot: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedClearanceCheckpoint): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<BatchedClearanceCheckpoint>): MutableList<BatchedClearanceCheckpoint> {
        val matched = filter(items) { candidate: BatchedClearanceCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedClearanceCheckpoints(store: BatchedClearanceCheckpointStore, query: BatchedClearanceCheckpointQuery): MutableList<BatchedClearanceCheckpoint> {
    return query.apply(store.list())
}
