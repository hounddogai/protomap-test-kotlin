package com.acme.transport.customs.declaration.agreement

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched declaration checkpoints by code. */
interface BatchedDeclarationCheckpointStore {
    fun find(code: String): BatchedDeclarationCheckpoint?
    fun save(item: BatchedDeclarationCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDeclarationCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDeclarationCheckpointStore(
    var items: MutableMap<String, BatchedDeclarationCheckpoint>,
    var order: MutableList<String>,
) : BatchedDeclarationCheckpointStore {
    override fun find(code: String): BatchedDeclarationCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDeclarationCheckpoint) {
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

    override fun list(): MutableList<BatchedDeclarationCheckpoint> {
        val out = mutableListOf<BatchedDeclarationCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDeclarationCheckpointStore(): BatchedDeclarationCheckpointStore {
    return MemoryBatchedDeclarationCheckpointStore(
        mutableMapOf<String, BatchedDeclarationCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDeclarationCheckpointStore(
    var inner: BatchedDeclarationCheckpointStore,
    var cache: TtlCache<BatchedDeclarationCheckpoint>,
    var hits: Int,
    var misses: Int,
) : BatchedDeclarationCheckpointStore {
    override fun find(code: String): BatchedDeclarationCheckpoint? {
        val hit = (cache.get(code) as BatchedDeclarationCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDeclarationCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDeclarationCheckpoint> = inner.list()
}

fun newCachedBatchedDeclarationCheckpointStore(inner: BatchedDeclarationCheckpointStore, ttl: Int, clock: Clock): BatchedDeclarationCheckpointStore {
    return CachedBatchedDeclarationCheckpointStore(
        inner = inner,
        cache = newTtlCache<BatchedDeclarationCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched declaration checkpoints. Empty fields match all. */
data class BatchedDeclarationCheckpointQuery(
    var channel: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDeclarationCheckpoint): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDeclarationCheckpoint>): MutableList<BatchedDeclarationCheckpoint> {
        val matched = filter(items) { candidate: BatchedDeclarationCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDeclarationCheckpoints(store: BatchedDeclarationCheckpointStore, query: BatchedDeclarationCheckpointQuery): MutableList<BatchedDeclarationCheckpoint> {
    return query.apply(store.list())
}
