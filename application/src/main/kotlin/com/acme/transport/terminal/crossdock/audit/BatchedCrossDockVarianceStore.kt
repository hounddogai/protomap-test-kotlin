package com.acme.transport.terminal.crossdock.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched cross dock variances by code. */
interface BatchedCrossDockVarianceStore {
    fun find(code: String): BatchedCrossDockVariance?
    fun save(item: BatchedCrossDockVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCrossDockVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCrossDockVarianceStore(
    var items: MutableMap<String, BatchedCrossDockVariance>,
    var order: MutableList<String>,
) : BatchedCrossDockVarianceStore {
    override fun find(code: String): BatchedCrossDockVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCrossDockVariance) {
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

    override fun list(): MutableList<BatchedCrossDockVariance> {
        val out = mutableListOf<BatchedCrossDockVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCrossDockVarianceStore(): BatchedCrossDockVarianceStore {
    return MemoryBatchedCrossDockVarianceStore(
        mutableMapOf<String, BatchedCrossDockVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCrossDockVarianceStore(
    var inner: BatchedCrossDockVarianceStore,
    var cache: TtlCache<BatchedCrossDockVariance>,
    var hits: Int,
    var misses: Int,
) : BatchedCrossDockVarianceStore {
    override fun find(code: String): BatchedCrossDockVariance? {
        val hit = (cache.get(code) as BatchedCrossDockVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCrossDockVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCrossDockVariance> = inner.list()
}

fun newCachedBatchedCrossDockVarianceStore(inner: BatchedCrossDockVarianceStore, ttl: Int, clock: Clock): BatchedCrossDockVarianceStore {
    return CachedBatchedCrossDockVarianceStore(
        inner = inner,
        cache = newTtlCache<BatchedCrossDockVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched cross dock variances. Empty fields match all. */
data class BatchedCrossDockVarianceQuery(
    var category: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCrossDockVariance): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedCrossDockVariance>): MutableList<BatchedCrossDockVariance> {
        val matched = filter(items) { candidate: BatchedCrossDockVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCrossDockVariances(store: BatchedCrossDockVarianceStore, query: BatchedCrossDockVarianceQuery): MutableList<BatchedCrossDockVariance> {
    return query.apply(store.list())
}
