package com.acme.transport.network.node.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched node requests by code. */
interface BatchedNodeRequestStore {
    fun find(code: String): BatchedNodeRequest?
    fun save(item: BatchedNodeRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedNodeRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedNodeRequestStore(
    var items: MutableMap<String, BatchedNodeRequest>,
    var order: MutableList<String>,
) : BatchedNodeRequestStore {
    override fun find(code: String): BatchedNodeRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedNodeRequest) {
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

    override fun list(): MutableList<BatchedNodeRequest> {
        val out = mutableListOf<BatchedNodeRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedNodeRequestStore(): BatchedNodeRequestStore {
    return MemoryBatchedNodeRequestStore(mutableMapOf<String, BatchedNodeRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedNodeRequestStore(
    var inner: BatchedNodeRequestStore,
    var cache: TtlCache<BatchedNodeRequest>,
    var hits: Int,
    var misses: Int,
) : BatchedNodeRequestStore {
    override fun find(code: String): BatchedNodeRequest? {
        val hit = (cache.get(code) as BatchedNodeRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedNodeRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedNodeRequest> = inner.list()
}

fun newCachedBatchedNodeRequestStore(inner: BatchedNodeRequestStore, ttl: Int, clock: Clock): BatchedNodeRequestStore {
    return CachedBatchedNodeRequestStore(
        inner = inner,
        cache = newTtlCache<BatchedNodeRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched node requests. Empty fields match all. */
data class BatchedNodeRequestQuery(
    var channel: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedNodeRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedNodeRequest>): MutableList<BatchedNodeRequest> {
        val matched = filter(items) { candidate: BatchedNodeRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedNodeRequests(store: BatchedNodeRequestStore, query: BatchedNodeRequestQuery): MutableList<BatchedNodeRequest> {
    return query.apply(store.list())
}
