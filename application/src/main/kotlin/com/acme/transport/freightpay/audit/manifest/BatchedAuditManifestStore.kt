package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched audit manifests by code. */
interface BatchedAuditManifestStore {
    fun find(code: String): BatchedAuditManifest?
    fun save(item: BatchedAuditManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAuditManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAuditManifestStore(
    var items: MutableMap<String, BatchedAuditManifest>,
    var order: MutableList<String>,
) : BatchedAuditManifestStore {
    override fun find(code: String): BatchedAuditManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAuditManifest) {
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

    override fun list(): MutableList<BatchedAuditManifest> {
        val out = mutableListOf<BatchedAuditManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAuditManifestStore(): BatchedAuditManifestStore {
    return MemoryBatchedAuditManifestStore(
        mutableMapOf<String, BatchedAuditManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAuditManifestStore(
    var inner: BatchedAuditManifestStore,
    var cache: TtlCache<BatchedAuditManifest>,
    var hits: Int,
    var misses: Int,
) : BatchedAuditManifestStore {
    override fun find(code: String): BatchedAuditManifest? {
        val hit = (cache.get(code) as BatchedAuditManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAuditManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAuditManifest> = inner.list()
}

fun newCachedBatchedAuditManifestStore(inner: BatchedAuditManifestStore, ttl: Int, clock: Clock): BatchedAuditManifestStore {
    return CachedBatchedAuditManifestStore(
        inner = inner,
        cache = newTtlCache<BatchedAuditManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched audit manifests. Empty fields match all. */
data class BatchedAuditManifestQuery(
    var lane: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAuditManifest): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAuditManifest>): MutableList<BatchedAuditManifest> {
        val matched = filter(items) { candidate: BatchedAuditManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAuditManifests(store: BatchedAuditManifestStore, query: BatchedAuditManifestQuery): MutableList<BatchedAuditManifest> {
    return query.apply(store.list())
}
