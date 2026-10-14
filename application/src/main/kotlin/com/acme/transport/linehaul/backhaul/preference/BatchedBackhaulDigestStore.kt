package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched backhaul digests by code. */
interface BatchedBackhaulDigestStore {
    fun find(code: String): BatchedBackhaulDigest?
    fun save(item: BatchedBackhaulDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedBackhaulDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedBackhaulDigestStore(
    var items: MutableMap<String, BatchedBackhaulDigest>,
    var order: MutableList<String>,
) : BatchedBackhaulDigestStore {
    override fun find(code: String): BatchedBackhaulDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedBackhaulDigest) {
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

    override fun list(): MutableList<BatchedBackhaulDigest> {
        val out = mutableListOf<BatchedBackhaulDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedBackhaulDigestStore(): BatchedBackhaulDigestStore {
    return MemoryBatchedBackhaulDigestStore(
        mutableMapOf<String, BatchedBackhaulDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedBackhaulDigestStore(
    var inner: BatchedBackhaulDigestStore,
    var cache: TtlCache<BatchedBackhaulDigest>,
    var hits: Int,
    var misses: Int,
) : BatchedBackhaulDigestStore {
    override fun find(code: String): BatchedBackhaulDigest? {
        val hit = (cache.get(code) as BatchedBackhaulDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedBackhaulDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedBackhaulDigest> = inner.list()
}

fun newCachedBatchedBackhaulDigestStore(inner: BatchedBackhaulDigestStore, ttl: Int, clock: Clock): BatchedBackhaulDigestStore {
    return CachedBatchedBackhaulDigestStore(
        inner = inner,
        cache = newTtlCache<BatchedBackhaulDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched backhaul digests. Empty fields match all. */
data class BatchedBackhaulDigestQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedBackhaulDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedBackhaulDigest>): MutableList<BatchedBackhaulDigest> {
        val matched = filter(items) { candidate: BatchedBackhaulDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedBackhaulDigests(store: BatchedBackhaulDigestStore, query: BatchedBackhaulDigestQuery): MutableList<BatchedBackhaulDigest> {
    return query.apply(store.list())
}
