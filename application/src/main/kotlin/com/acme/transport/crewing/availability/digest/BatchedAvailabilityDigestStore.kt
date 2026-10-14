package com.acme.transport.crewing.availability.digest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched availability digests by code. */
interface BatchedAvailabilityDigestStore {
    fun find(code: String): BatchedAvailabilityDigest?
    fun save(item: BatchedAvailabilityDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAvailabilityDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAvailabilityDigestStore(
    var items: MutableMap<String, BatchedAvailabilityDigest>,
    var order: MutableList<String>,
) : BatchedAvailabilityDigestStore {
    override fun find(code: String): BatchedAvailabilityDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAvailabilityDigest) {
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

    override fun list(): MutableList<BatchedAvailabilityDigest> {
        val out = mutableListOf<BatchedAvailabilityDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAvailabilityDigestStore(): BatchedAvailabilityDigestStore {
    return MemoryBatchedAvailabilityDigestStore(
        mutableMapOf<String, BatchedAvailabilityDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAvailabilityDigestStore(
    var inner: BatchedAvailabilityDigestStore,
    var cache: TtlCache<BatchedAvailabilityDigest>,
    var hits: Int,
    var misses: Int,
) : BatchedAvailabilityDigestStore {
    override fun find(code: String): BatchedAvailabilityDigest? {
        val hit = (cache.get(code) as BatchedAvailabilityDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAvailabilityDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAvailabilityDigest> = inner.list()
}

fun newCachedBatchedAvailabilityDigestStore(inner: BatchedAvailabilityDigestStore, ttl: Int, clock: Clock): BatchedAvailabilityDigestStore {
    return CachedBatchedAvailabilityDigestStore(
        inner = inner,
        cache = newTtlCache<BatchedAvailabilityDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched availability digests. Empty fields match all. */
data class BatchedAvailabilityDigestQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAvailabilityDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAvailabilityDigest>): MutableList<BatchedAvailabilityDigest> {
        val matched = filter(items) { candidate: BatchedAvailabilityDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAvailabilityDigests(store: BatchedAvailabilityDigestStore, query: BatchedAvailabilityDigestQuery): MutableList<BatchedAvailabilityDigest> {
    return query.apply(store.list())
}
