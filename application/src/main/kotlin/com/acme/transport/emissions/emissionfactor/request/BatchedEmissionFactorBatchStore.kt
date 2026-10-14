package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched emission factor batches by code. */
interface BatchedEmissionFactorBatchStore {
    fun find(code: String): BatchedEmissionFactorBatch?
    fun save(item: BatchedEmissionFactorBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedEmissionFactorBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedEmissionFactorBatchStore(
    var items: MutableMap<String, BatchedEmissionFactorBatch>,
    var order: MutableList<String>,
) : BatchedEmissionFactorBatchStore {
    override fun find(code: String): BatchedEmissionFactorBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedEmissionFactorBatch) {
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

    override fun list(): MutableList<BatchedEmissionFactorBatch> {
        val out = mutableListOf<BatchedEmissionFactorBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedEmissionFactorBatchStore(): BatchedEmissionFactorBatchStore {
    return MemoryBatchedEmissionFactorBatchStore(
        mutableMapOf<String, BatchedEmissionFactorBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedEmissionFactorBatchStore(
    var inner: BatchedEmissionFactorBatchStore,
    var cache: TtlCache<BatchedEmissionFactorBatch>,
    var hits: Int,
    var misses: Int,
) : BatchedEmissionFactorBatchStore {
    override fun find(code: String): BatchedEmissionFactorBatch? {
        val hit = (cache.get(code) as BatchedEmissionFactorBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedEmissionFactorBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedEmissionFactorBatch> = inner.list()
}

fun newCachedBatchedEmissionFactorBatchStore(inner: BatchedEmissionFactorBatchStore, ttl: Int, clock: Clock): BatchedEmissionFactorBatchStore {
    return CachedBatchedEmissionFactorBatchStore(
        inner = inner,
        cache = newTtlCache<BatchedEmissionFactorBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched emission factor batches. Empty fields match all. */
data class BatchedEmissionFactorBatchQuery(
    var depot: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedEmissionFactorBatch): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedEmissionFactorBatch>): MutableList<BatchedEmissionFactorBatch> {
        val matched = filter(items) { candidate: BatchedEmissionFactorBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedEmissionFactorBatches(store: BatchedEmissionFactorBatchStore, query: BatchedEmissionFactorBatchQuery): MutableList<BatchedEmissionFactorBatch> {
    return query.apply(store.list())
}
