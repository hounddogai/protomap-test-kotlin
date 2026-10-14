package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched qualification checkpoints by code. */
interface BatchedQualificationCheckpointStore {
    fun find(code: String): BatchedQualificationCheckpoint?
    fun save(item: BatchedQualificationCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedQualificationCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedQualificationCheckpointStore(
    var items: MutableMap<String, BatchedQualificationCheckpoint>,
    var order: MutableList<String>,
) : BatchedQualificationCheckpointStore {
    override fun find(code: String): BatchedQualificationCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedQualificationCheckpoint) {
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

    override fun list(): MutableList<BatchedQualificationCheckpoint> {
        val out = mutableListOf<BatchedQualificationCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedQualificationCheckpointStore(): BatchedQualificationCheckpointStore {
    return MemoryBatchedQualificationCheckpointStore(
        mutableMapOf<String, BatchedQualificationCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedQualificationCheckpointStore(
    var inner: BatchedQualificationCheckpointStore,
    var cache: TtlCache<BatchedQualificationCheckpoint>,
    var hits: Int,
    var misses: Int,
) : BatchedQualificationCheckpointStore {
    override fun find(code: String): BatchedQualificationCheckpoint? {
        val hit = (cache.get(code) as BatchedQualificationCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedQualificationCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedQualificationCheckpoint> = inner.list()
}

fun newCachedBatchedQualificationCheckpointStore(inner: BatchedQualificationCheckpointStore, ttl: Int, clock: Clock): BatchedQualificationCheckpointStore {
    return CachedBatchedQualificationCheckpointStore(
        inner = inner,
        cache = newTtlCache<BatchedQualificationCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched qualification checkpoints. Empty fields match all. */
data class BatchedQualificationCheckpointQuery(
    var unit: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedQualificationCheckpoint): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedQualificationCheckpoint>): MutableList<BatchedQualificationCheckpoint> {
        val matched = filter(items) { candidate: BatchedQualificationCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedQualificationCheckpoints(store: BatchedQualificationCheckpointStore, query: BatchedQualificationCheckpointQuery): MutableList<BatchedQualificationCheckpoint> {
    return query.apply(store.list())
}
