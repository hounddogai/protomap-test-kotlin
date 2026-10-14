package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched proof of delivery mappings by code. */
interface BatchedProofOfDeliveryMappingStore {
    fun find(code: String): BatchedProofOfDeliveryMapping?
    fun save(item: BatchedProofOfDeliveryMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedProofOfDeliveryMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedProofOfDeliveryMappingStore(
    var items: MutableMap<String, BatchedProofOfDeliveryMapping>,
    var order: MutableList<String>,
) : BatchedProofOfDeliveryMappingStore {
    override fun find(code: String): BatchedProofOfDeliveryMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedProofOfDeliveryMapping) {
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

    override fun list(): MutableList<BatchedProofOfDeliveryMapping> {
        val out = mutableListOf<BatchedProofOfDeliveryMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedProofOfDeliveryMappingStore(): BatchedProofOfDeliveryMappingStore {
    return MemoryBatchedProofOfDeliveryMappingStore(
        mutableMapOf<String, BatchedProofOfDeliveryMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedProofOfDeliveryMappingStore(
    var inner: BatchedProofOfDeliveryMappingStore,
    var cache: TtlCache<BatchedProofOfDeliveryMapping>,
    var hits: Int,
    var misses: Int,
) : BatchedProofOfDeliveryMappingStore {
    override fun find(code: String): BatchedProofOfDeliveryMapping? {
        val hit = (cache.get(code) as BatchedProofOfDeliveryMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedProofOfDeliveryMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedProofOfDeliveryMapping> = inner.list()
}

fun newCachedBatchedProofOfDeliveryMappingStore(inner: BatchedProofOfDeliveryMappingStore, ttl: Int, clock: Clock): BatchedProofOfDeliveryMappingStore {
    return CachedBatchedProofOfDeliveryMappingStore(
        inner = inner,
        cache = newTtlCache<BatchedProofOfDeliveryMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched proof of delivery mappings. Empty fields match all. */
data class BatchedProofOfDeliveryMappingQuery(
    var country: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedProofOfDeliveryMapping): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<BatchedProofOfDeliveryMapping>): MutableList<BatchedProofOfDeliveryMapping> {
        val matched = filter(items) { candidate: BatchedProofOfDeliveryMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedProofOfDeliveryMappings(store: BatchedProofOfDeliveryMappingStore, query: BatchedProofOfDeliveryMappingQuery): MutableList<BatchedProofOfDeliveryMapping> {
    return query.apply(store.list())
}
