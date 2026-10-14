package com.acme.transport.fleetops.defect.notice

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched defect digests by code. */
interface BatchedDefectDigestStore {
    fun find(code: String): BatchedDefectDigest?
    fun save(item: BatchedDefectDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDefectDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDefectDigestStore(
    var items: MutableMap<String, BatchedDefectDigest>,
    var order: MutableList<String>,
) : BatchedDefectDigestStore {
    override fun find(code: String): BatchedDefectDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDefectDigest) {
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

    override fun list(): MutableList<BatchedDefectDigest> {
        val out = mutableListOf<BatchedDefectDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDefectDigestStore(): BatchedDefectDigestStore {
    return MemoryBatchedDefectDigestStore(mutableMapOf<String, BatchedDefectDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDefectDigestStore(
    var inner: BatchedDefectDigestStore,
    var cache: TtlCache<BatchedDefectDigest>,
    var hits: Int,
    var misses: Int,
) : BatchedDefectDigestStore {
    override fun find(code: String): BatchedDefectDigest? {
        val hit = (cache.get(code) as BatchedDefectDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDefectDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDefectDigest> = inner.list()
}

fun newCachedBatchedDefectDigestStore(inner: BatchedDefectDigestStore, ttl: Int, clock: Clock): BatchedDefectDigestStore {
    return CachedBatchedDefectDigestStore(
        inner = inner,
        cache = newTtlCache<BatchedDefectDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched defect digests. Empty fields match all. */
data class BatchedDefectDigestQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDefectDigest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDefectDigest>): MutableList<BatchedDefectDigest> {
        val matched = filter(items) { candidate: BatchedDefectDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDefectDigests(store: BatchedDefectDigestStore, query: BatchedDefectDigestQuery): MutableList<BatchedDefectDigest> {
    return query.apply(store.list())
}
