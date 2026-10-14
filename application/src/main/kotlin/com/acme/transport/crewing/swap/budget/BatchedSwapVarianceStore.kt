package com.acme.transport.crewing.swap.budget

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched swap variances by code. */
interface BatchedSwapVarianceStore {
    fun find(code: String): BatchedSwapVariance?
    fun save(item: BatchedSwapVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedSwapVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedSwapVarianceStore(
    var items: MutableMap<String, BatchedSwapVariance>,
    var order: MutableList<String>,
) : BatchedSwapVarianceStore {
    override fun find(code: String): BatchedSwapVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedSwapVariance) {
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

    override fun list(): MutableList<BatchedSwapVariance> {
        val out = mutableListOf<BatchedSwapVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedSwapVarianceStore(): BatchedSwapVarianceStore {
    return MemoryBatchedSwapVarianceStore(mutableMapOf<String, BatchedSwapVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedSwapVarianceStore(
    var inner: BatchedSwapVarianceStore,
    var cache: TtlCache<BatchedSwapVariance>,
    var hits: Int,
    var misses: Int,
) : BatchedSwapVarianceStore {
    override fun find(code: String): BatchedSwapVariance? {
        val hit = (cache.get(code) as BatchedSwapVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedSwapVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedSwapVariance> = inner.list()
}

fun newCachedBatchedSwapVarianceStore(inner: BatchedSwapVarianceStore, ttl: Int, clock: Clock): BatchedSwapVarianceStore {
    return CachedBatchedSwapVarianceStore(
        inner = inner,
        cache = newTtlCache<BatchedSwapVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched swap variances. Empty fields match all. */
data class BatchedSwapVarianceQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedSwapVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedSwapVariance>): MutableList<BatchedSwapVariance> {
        val matched = filter(items) { candidate: BatchedSwapVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedSwapVariances(store: BatchedSwapVarianceStore, query: BatchedSwapVarianceQuery): MutableList<BatchedSwapVariance> {
    return query.apply(store.list())
}
