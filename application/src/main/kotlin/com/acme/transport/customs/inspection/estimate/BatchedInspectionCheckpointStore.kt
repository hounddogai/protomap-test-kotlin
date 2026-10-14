package com.acme.transport.customs.inspection.estimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched inspection checkpoints by code. */
interface BatchedInspectionCheckpointStore {
    fun find(code: String): BatchedInspectionCheckpoint?
    fun save(item: BatchedInspectionCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedInspectionCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedInspectionCheckpointStore(
    var items: MutableMap<String, BatchedInspectionCheckpoint>,
    var order: MutableList<String>,
) : BatchedInspectionCheckpointStore {
    override fun find(code: String): BatchedInspectionCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedInspectionCheckpoint) {
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

    override fun list(): MutableList<BatchedInspectionCheckpoint> {
        val out = mutableListOf<BatchedInspectionCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedInspectionCheckpointStore(): BatchedInspectionCheckpointStore {
    return MemoryBatchedInspectionCheckpointStore(
        mutableMapOf<String, BatchedInspectionCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedInspectionCheckpointStore(
    var inner: BatchedInspectionCheckpointStore,
    var cache: TtlCache<BatchedInspectionCheckpoint>,
    var hits: Int,
    var misses: Int,
) : BatchedInspectionCheckpointStore {
    override fun find(code: String): BatchedInspectionCheckpoint? {
        val hit = (cache.get(code) as BatchedInspectionCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedInspectionCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedInspectionCheckpoint> = inner.list()
}

fun newCachedBatchedInspectionCheckpointStore(inner: BatchedInspectionCheckpointStore, ttl: Int, clock: Clock): BatchedInspectionCheckpointStore {
    return CachedBatchedInspectionCheckpointStore(
        inner = inner,
        cache = newTtlCache<BatchedInspectionCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched inspection checkpoints. Empty fields match all. */
data class BatchedInspectionCheckpointQuery(
    var source: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedInspectionCheckpoint): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedInspectionCheckpoint>): MutableList<BatchedInspectionCheckpoint> {
        val matched = filter(items) { candidate: BatchedInspectionCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedInspectionCheckpoints(store: BatchedInspectionCheckpointStore, query: BatchedInspectionCheckpointQuery): MutableList<BatchedInspectionCheckpoint> {
    return query.apply(store.list())
}
