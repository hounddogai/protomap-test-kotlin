package com.acme.transport.safety.nearmiss.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched near miss variances by code. */
interface BatchedNearMissVarianceStore {
    fun find(code: String): BatchedNearMissVariance?
    fun save(item: BatchedNearMissVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedNearMissVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedNearMissVarianceStore(
    var items: MutableMap<String, BatchedNearMissVariance>,
    var order: MutableList<String>,
) : BatchedNearMissVarianceStore {
    override fun find(code: String): BatchedNearMissVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedNearMissVariance) {
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

    override fun list(): MutableList<BatchedNearMissVariance> {
        val out = mutableListOf<BatchedNearMissVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedNearMissVarianceStore(): BatchedNearMissVarianceStore {
    return MemoryBatchedNearMissVarianceStore(
        mutableMapOf<String, BatchedNearMissVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedNearMissVarianceStore(
    var inner: BatchedNearMissVarianceStore,
    var cache: TtlCache<BatchedNearMissVariance>,
    var hits: Int,
    var misses: Int,
) : BatchedNearMissVarianceStore {
    override fun find(code: String): BatchedNearMissVariance? {
        val hit = (cache.get(code) as BatchedNearMissVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedNearMissVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedNearMissVariance> = inner.list()
}

fun newCachedBatchedNearMissVarianceStore(inner: BatchedNearMissVarianceStore, ttl: Int, clock: Clock): BatchedNearMissVarianceStore {
    return CachedBatchedNearMissVarianceStore(
        inner = inner,
        cache = newTtlCache<BatchedNearMissVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched near miss variances. Empty fields match all. */
data class BatchedNearMissVarianceQuery(
    var region: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedNearMissVariance): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<BatchedNearMissVariance>): MutableList<BatchedNearMissVariance> {
        val matched = filter(items) { candidate: BatchedNearMissVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedNearMissVariances(store: BatchedNearMissVarianceStore, query: BatchedNearMissVarianceQuery): MutableList<BatchedNearMissVariance> {
    return query.apply(store.list())
}
