package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched transit time requests by code. */
interface BatchedTransitTimeRequestStore {
    fun find(code: String): BatchedTransitTimeRequest?
    fun save(item: BatchedTransitTimeRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTransitTimeRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTransitTimeRequestStore(
    var items: MutableMap<String, BatchedTransitTimeRequest>,
    var order: MutableList<String>,
) : BatchedTransitTimeRequestStore {
    override fun find(code: String): BatchedTransitTimeRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTransitTimeRequest) {
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

    override fun list(): MutableList<BatchedTransitTimeRequest> {
        val out = mutableListOf<BatchedTransitTimeRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTransitTimeRequestStore(): BatchedTransitTimeRequestStore {
    return MemoryBatchedTransitTimeRequestStore(
        mutableMapOf<String, BatchedTransitTimeRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTransitTimeRequestStore(
    var inner: BatchedTransitTimeRequestStore,
    var cache: TtlCache<BatchedTransitTimeRequest>,
    var hits: Int,
    var misses: Int,
) : BatchedTransitTimeRequestStore {
    override fun find(code: String): BatchedTransitTimeRequest? {
        val hit = (cache.get(code) as BatchedTransitTimeRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTransitTimeRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTransitTimeRequest> = inner.list()
}

fun newCachedBatchedTransitTimeRequestStore(inner: BatchedTransitTimeRequestStore, ttl: Int, clock: Clock): BatchedTransitTimeRequestStore {
    return CachedBatchedTransitTimeRequestStore(
        inner = inner,
        cache = newTtlCache<BatchedTransitTimeRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched transit time requests. Empty fields match all. */
data class BatchedTransitTimeRequestQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTransitTimeRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<BatchedTransitTimeRequest>): MutableList<BatchedTransitTimeRequest> {
        val matched = filter(items) { candidate: BatchedTransitTimeRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTransitTimeRequests(store: BatchedTransitTimeRequestStore, query: BatchedTransitTimeRequestQuery): MutableList<BatchedTransitTimeRequest> {
    return query.apply(store.list())
}
