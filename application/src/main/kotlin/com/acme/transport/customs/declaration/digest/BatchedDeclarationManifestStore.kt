package com.acme.transport.customs.declaration.digest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched declaration manifests by code. */
interface BatchedDeclarationManifestStore {
    fun find(code: String): BatchedDeclarationManifest?
    fun save(item: BatchedDeclarationManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDeclarationManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDeclarationManifestStore(
    var items: MutableMap<String, BatchedDeclarationManifest>,
    var order: MutableList<String>,
) : BatchedDeclarationManifestStore {
    override fun find(code: String): BatchedDeclarationManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDeclarationManifest) {
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

    override fun list(): MutableList<BatchedDeclarationManifest> {
        val out = mutableListOf<BatchedDeclarationManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDeclarationManifestStore(): BatchedDeclarationManifestStore {
    return MemoryBatchedDeclarationManifestStore(
        mutableMapOf<String, BatchedDeclarationManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDeclarationManifestStore(
    var inner: BatchedDeclarationManifestStore,
    var cache: TtlCache<BatchedDeclarationManifest>,
    var hits: Int,
    var misses: Int,
) : BatchedDeclarationManifestStore {
    override fun find(code: String): BatchedDeclarationManifest? {
        val hit = (cache.get(code) as BatchedDeclarationManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDeclarationManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDeclarationManifest> = inner.list()
}

fun newCachedBatchedDeclarationManifestStore(inner: BatchedDeclarationManifestStore, ttl: Int, clock: Clock): BatchedDeclarationManifestStore {
    return CachedBatchedDeclarationManifestStore(
        inner = inner,
        cache = newTtlCache<BatchedDeclarationManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched declaration manifests. Empty fields match all. */
data class BatchedDeclarationManifestQuery(
    var channel: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDeclarationManifest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDeclarationManifest>): MutableList<BatchedDeclarationManifest> {
        val matched = filter(items) { candidate: BatchedDeclarationManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDeclarationManifests(store: BatchedDeclarationManifestStore, query: BatchedDeclarationManifestQuery): MutableList<BatchedDeclarationManifest> {
    return query.apply(store.list())
}
