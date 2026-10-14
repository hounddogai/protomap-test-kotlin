package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched dock door checkpoints by code. */
interface BatchedDockDoorCheckpointStore {
    fun find(code: String): BatchedDockDoorCheckpoint?
    fun save(item: BatchedDockDoorCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDockDoorCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDockDoorCheckpointStore(
    var items: MutableMap<String, BatchedDockDoorCheckpoint>,
    var order: MutableList<String>,
) : BatchedDockDoorCheckpointStore {
    override fun find(code: String): BatchedDockDoorCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDockDoorCheckpoint) {
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

    override fun list(): MutableList<BatchedDockDoorCheckpoint> {
        val out = mutableListOf<BatchedDockDoorCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDockDoorCheckpointStore(): BatchedDockDoorCheckpointStore {
    return MemoryBatchedDockDoorCheckpointStore(
        mutableMapOf<String, BatchedDockDoorCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDockDoorCheckpointStore(
    var inner: BatchedDockDoorCheckpointStore,
    var cache: TtlCache<BatchedDockDoorCheckpoint>,
    var hits: Int,
    var misses: Int,
) : BatchedDockDoorCheckpointStore {
    override fun find(code: String): BatchedDockDoorCheckpoint? {
        val hit = (cache.get(code) as BatchedDockDoorCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDockDoorCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDockDoorCheckpoint> = inner.list()
}

fun newCachedBatchedDockDoorCheckpointStore(inner: BatchedDockDoorCheckpointStore, ttl: Int, clock: Clock): BatchedDockDoorCheckpointStore {
    return CachedBatchedDockDoorCheckpointStore(
        inner = inner,
        cache = newTtlCache<BatchedDockDoorCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched dock door checkpoints. Empty fields match all. */
data class BatchedDockDoorCheckpointQuery(
    var source: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDockDoorCheckpoint): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDockDoorCheckpoint>): MutableList<BatchedDockDoorCheckpoint> {
        val matched = filter(items) { candidate: BatchedDockDoorCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDockDoorCheckpoints(store: BatchedDockDoorCheckpointStore, query: BatchedDockDoorCheckpointQuery): MutableList<BatchedDockDoorCheckpoint> {
    return query.apply(store.list())
}
