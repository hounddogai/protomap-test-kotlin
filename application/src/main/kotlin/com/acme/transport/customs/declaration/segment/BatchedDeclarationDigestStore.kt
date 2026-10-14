package com.acme.transport.customs.declaration.segment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched declaration digests by code. */
interface BatchedDeclarationDigestStore {
    fun find(code: String): BatchedDeclarationDigest?
    fun save(item: BatchedDeclarationDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDeclarationDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDeclarationDigestStore(
    var items: MutableMap<String, BatchedDeclarationDigest>,
    var order: MutableList<String>,
) : BatchedDeclarationDigestStore {
    override fun find(code: String): BatchedDeclarationDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDeclarationDigest) {
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

    override fun list(): MutableList<BatchedDeclarationDigest> {
        val out = mutableListOf<BatchedDeclarationDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDeclarationDigestStore(): BatchedDeclarationDigestStore {
    return MemoryBatchedDeclarationDigestStore(
        mutableMapOf<String, BatchedDeclarationDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDeclarationDigestStore(
    var inner: BatchedDeclarationDigestStore,
    var cache: TtlCache<BatchedDeclarationDigest>,
    var hits: Int,
    var misses: Int,
) : BatchedDeclarationDigestStore {
    override fun find(code: String): BatchedDeclarationDigest? {
        val hit = (cache.get(code) as BatchedDeclarationDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDeclarationDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDeclarationDigest> = inner.list()
}

fun newCachedBatchedDeclarationDigestStore(inner: BatchedDeclarationDigestStore, ttl: Int, clock: Clock): BatchedDeclarationDigestStore {
    return CachedBatchedDeclarationDigestStore(
        inner = inner,
        cache = newTtlCache<BatchedDeclarationDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched declaration digests. Empty fields match all. */
data class BatchedDeclarationDigestQuery(
    var locale: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDeclarationDigest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<BatchedDeclarationDigest>): MutableList<BatchedDeclarationDigest> {
        val matched = filter(items) { candidate: BatchedDeclarationDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDeclarationDigests(store: BatchedDeclarationDigestStore, query: BatchedDeclarationDigestQuery): MutableList<BatchedDeclarationDigest> {
    return query.apply(store.list())
}
