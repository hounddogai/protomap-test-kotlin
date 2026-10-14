package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched proof of delivery manifests by code. */
interface BatchedProofOfDeliveryManifestStore {
    fun find(code: String): BatchedProofOfDeliveryManifest?
    fun save(item: BatchedProofOfDeliveryManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedProofOfDeliveryManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedProofOfDeliveryManifestStore(
    var items: MutableMap<String, BatchedProofOfDeliveryManifest>,
    var order: MutableList<String>,
) : BatchedProofOfDeliveryManifestStore {
    override fun find(code: String): BatchedProofOfDeliveryManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedProofOfDeliveryManifest) {
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

    override fun list(): MutableList<BatchedProofOfDeliveryManifest> {
        val out = mutableListOf<BatchedProofOfDeliveryManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedProofOfDeliveryManifestStore(): BatchedProofOfDeliveryManifestStore {
    return MemoryBatchedProofOfDeliveryManifestStore(
        mutableMapOf<String, BatchedProofOfDeliveryManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedProofOfDeliveryManifestStore(
    var inner: BatchedProofOfDeliveryManifestStore,
    var cache: TtlCache<BatchedProofOfDeliveryManifest>,
    var hits: Int,
    var misses: Int,
) : BatchedProofOfDeliveryManifestStore {
    override fun find(code: String): BatchedProofOfDeliveryManifest? {
        val hit = (cache.get(code) as BatchedProofOfDeliveryManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedProofOfDeliveryManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedProofOfDeliveryManifest> = inner.list()
}

fun newCachedBatchedProofOfDeliveryManifestStore(inner: BatchedProofOfDeliveryManifestStore, ttl: Int, clock: Clock): BatchedProofOfDeliveryManifestStore {
    return CachedBatchedProofOfDeliveryManifestStore(
        inner = inner,
        cache = newTtlCache<BatchedProofOfDeliveryManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched proof of delivery manifests. Empty fields match all. */
data class BatchedProofOfDeliveryManifestQuery(
    var country: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedProofOfDeliveryManifest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<BatchedProofOfDeliveryManifest>): MutableList<BatchedProofOfDeliveryManifest> {
        val matched = filter(items) { candidate: BatchedProofOfDeliveryManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedProofOfDeliveryManifests(store: BatchedProofOfDeliveryManifestStore, query: BatchedProofOfDeliveryManifestQuery): MutableList<BatchedProofOfDeliveryManifest> {
    return query.apply(store.list())
}
