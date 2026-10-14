package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched proof of delivery rollups by code. */
interface BatchedProofOfDeliveryRollupStore {
    fun find(code: String): BatchedProofOfDeliveryRollup?
    fun save(item: BatchedProofOfDeliveryRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedProofOfDeliveryRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedProofOfDeliveryRollupStore(
    var items: MutableMap<String, BatchedProofOfDeliveryRollup>,
    var order: MutableList<String>,
) : BatchedProofOfDeliveryRollupStore {
    override fun find(code: String): BatchedProofOfDeliveryRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedProofOfDeliveryRollup) {
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

    override fun list(): MutableList<BatchedProofOfDeliveryRollup> {
        val out = mutableListOf<BatchedProofOfDeliveryRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedProofOfDeliveryRollupStore(): BatchedProofOfDeliveryRollupStore {
    return MemoryBatchedProofOfDeliveryRollupStore(
        mutableMapOf<String, BatchedProofOfDeliveryRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedProofOfDeliveryRollupStore(
    var inner: BatchedProofOfDeliveryRollupStore,
    var cache: TtlCache<BatchedProofOfDeliveryRollup>,
    var hits: Int,
    var misses: Int,
) : BatchedProofOfDeliveryRollupStore {
    override fun find(code: String): BatchedProofOfDeliveryRollup? {
        val hit = (cache.get(code) as BatchedProofOfDeliveryRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedProofOfDeliveryRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedProofOfDeliveryRollup> = inner.list()
}

fun newCachedBatchedProofOfDeliveryRollupStore(inner: BatchedProofOfDeliveryRollupStore, ttl: Int, clock: Clock): BatchedProofOfDeliveryRollupStore {
    return CachedBatchedProofOfDeliveryRollupStore(
        inner = inner,
        cache = newTtlCache<BatchedProofOfDeliveryRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched proof of delivery rollups. Empty fields match all. */
data class BatchedProofOfDeliveryRollupQuery(
    var unit: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedProofOfDeliveryRollup): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<BatchedProofOfDeliveryRollup>): MutableList<BatchedProofOfDeliveryRollup> {
        val matched = filter(items) { candidate: BatchedProofOfDeliveryRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedProofOfDeliveryRollups(store: BatchedProofOfDeliveryRollupStore, query: BatchedProofOfDeliveryRollupQuery): MutableList<BatchedProofOfDeliveryRollup> {
    return query.apply(store.list())
}
