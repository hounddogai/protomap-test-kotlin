package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched drop digests by code. */
interface BatchedDropDigestStore {
    fun find(code: String): BatchedDropDigest?
    fun save(item: BatchedDropDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDropDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDropDigestStore(
    var items: MutableMap<String, BatchedDropDigest>,
    var order: MutableList<String>,
) : BatchedDropDigestStore {
    override fun find(code: String): BatchedDropDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDropDigest) {
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

    override fun list(): MutableList<BatchedDropDigest> {
        val out = mutableListOf<BatchedDropDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDropDigestStore(): BatchedDropDigestStore {
    return MemoryBatchedDropDigestStore(mutableMapOf<String, BatchedDropDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDropDigestStore(
    var inner: BatchedDropDigestStore,
    var cache: TtlCache<BatchedDropDigest>,
    var hits: Int,
    var misses: Int,
) : BatchedDropDigestStore {
    override fun find(code: String): BatchedDropDigest? {
        val hit = (cache.get(code) as BatchedDropDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDropDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDropDigest> = inner.list()
}

fun newCachedBatchedDropDigestStore(inner: BatchedDropDigestStore, ttl: Int, clock: Clock): BatchedDropDigestStore {
    return CachedBatchedDropDigestStore(
        inner = inner,
        cache = newTtlCache<BatchedDropDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched drop digests. Empty fields match all. */
data class BatchedDropDigestQuery(
    var country: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDropDigest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDropDigest>): MutableList<BatchedDropDigest> {
        val matched = filter(items) { candidate: BatchedDropDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDropDigests(store: BatchedDropDigestStore, query: BatchedDropDigestQuery): MutableList<BatchedDropDigest> {
    return query.apply(store.list())
}
