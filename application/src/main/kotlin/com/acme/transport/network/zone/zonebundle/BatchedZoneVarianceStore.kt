package com.acme.transport.network.zone.zonebundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched zone variances by code. */
interface BatchedZoneVarianceStore {
    fun find(code: String): BatchedZoneVariance?
    fun save(item: BatchedZoneVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedZoneVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedZoneVarianceStore(
    var items: MutableMap<String, BatchedZoneVariance>,
    var order: MutableList<String>,
) : BatchedZoneVarianceStore {
    override fun find(code: String): BatchedZoneVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedZoneVariance) {
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

    override fun list(): MutableList<BatchedZoneVariance> {
        val out = mutableListOf<BatchedZoneVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedZoneVarianceStore(): BatchedZoneVarianceStore {
    return MemoryBatchedZoneVarianceStore(mutableMapOf<String, BatchedZoneVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedZoneVarianceStore(
    var inner: BatchedZoneVarianceStore,
    var cache: TtlCache<BatchedZoneVariance>,
    var hits: Int,
    var misses: Int,
) : BatchedZoneVarianceStore {
    override fun find(code: String): BatchedZoneVariance? {
        val hit = (cache.get(code) as BatchedZoneVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedZoneVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedZoneVariance> = inner.list()
}

fun newCachedBatchedZoneVarianceStore(inner: BatchedZoneVarianceStore, ttl: Int, clock: Clock): BatchedZoneVarianceStore {
    return CachedBatchedZoneVarianceStore(
        inner = inner,
        cache = newTtlCache<BatchedZoneVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched zone variances. Empty fields match all. */
data class BatchedZoneVarianceQuery(
    var segment: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedZoneVariance): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedZoneVariance>): MutableList<BatchedZoneVariance> {
        val matched = filter(items) { candidate: BatchedZoneVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedZoneVariances(store: BatchedZoneVarianceStore, query: BatchedZoneVarianceQuery): MutableList<BatchedZoneVariance> {
    return query.apply(store.list())
}
