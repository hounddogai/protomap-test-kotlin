package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched accessorial checkpoints by code. */
interface BatchedAccessorialCheckpointStore {
    fun find(code: String): BatchedAccessorialCheckpoint?
    fun save(item: BatchedAccessorialCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAccessorialCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAccessorialCheckpointStore(
    var items: MutableMap<String, BatchedAccessorialCheckpoint>,
    var order: MutableList<String>,
) : BatchedAccessorialCheckpointStore {
    override fun find(code: String): BatchedAccessorialCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAccessorialCheckpoint) {
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

    override fun list(): MutableList<BatchedAccessorialCheckpoint> {
        val out = mutableListOf<BatchedAccessorialCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAccessorialCheckpointStore(): BatchedAccessorialCheckpointStore {
    return MemoryBatchedAccessorialCheckpointStore(
        mutableMapOf<String, BatchedAccessorialCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAccessorialCheckpointStore(
    var inner: BatchedAccessorialCheckpointStore,
    var cache: TtlCache<BatchedAccessorialCheckpoint>,
    var hits: Int,
    var misses: Int,
) : BatchedAccessorialCheckpointStore {
    override fun find(code: String): BatchedAccessorialCheckpoint? {
        val hit = (cache.get(code) as BatchedAccessorialCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAccessorialCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAccessorialCheckpoint> = inner.list()
}

fun newCachedBatchedAccessorialCheckpointStore(inner: BatchedAccessorialCheckpointStore, ttl: Int, clock: Clock): BatchedAccessorialCheckpointStore {
    return CachedBatchedAccessorialCheckpointStore(
        inner = inner,
        cache = newTtlCache<BatchedAccessorialCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched accessorial checkpoints. Empty fields match all. */
data class BatchedAccessorialCheckpointQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAccessorialCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAccessorialCheckpoint>): MutableList<BatchedAccessorialCheckpoint> {
        val matched = filter(items) { candidate: BatchedAccessorialCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAccessorialCheckpoints(store: BatchedAccessorialCheckpointStore, query: BatchedAccessorialCheckpointQuery): MutableList<BatchedAccessorialCheckpoint> {
    return query.apply(store.list())
}
