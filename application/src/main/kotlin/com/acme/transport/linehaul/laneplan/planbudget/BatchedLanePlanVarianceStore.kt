package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched lane plan variances by code. */
interface BatchedLanePlanVarianceStore {
    fun find(code: String): BatchedLanePlanVariance?
    fun save(item: BatchedLanePlanVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedLanePlanVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedLanePlanVarianceStore(
    var items: MutableMap<String, BatchedLanePlanVariance>,
    var order: MutableList<String>,
) : BatchedLanePlanVarianceStore {
    override fun find(code: String): BatchedLanePlanVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedLanePlanVariance) {
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

    override fun list(): MutableList<BatchedLanePlanVariance> {
        val out = mutableListOf<BatchedLanePlanVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedLanePlanVarianceStore(): BatchedLanePlanVarianceStore {
    return MemoryBatchedLanePlanVarianceStore(
        mutableMapOf<String, BatchedLanePlanVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedLanePlanVarianceStore(
    var inner: BatchedLanePlanVarianceStore,
    var cache: TtlCache<BatchedLanePlanVariance>,
    var hits: Int,
    var misses: Int,
) : BatchedLanePlanVarianceStore {
    override fun find(code: String): BatchedLanePlanVariance? {
        val hit = (cache.get(code) as BatchedLanePlanVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedLanePlanVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedLanePlanVariance> = inner.list()
}

fun newCachedBatchedLanePlanVarianceStore(inner: BatchedLanePlanVarianceStore, ttl: Int, clock: Clock): BatchedLanePlanVarianceStore {
    return CachedBatchedLanePlanVarianceStore(
        inner = inner,
        cache = newTtlCache<BatchedLanePlanVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched lane plan variances. Empty fields match all. */
data class BatchedLanePlanVarianceQuery(
    var segment: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedLanePlanVariance): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedLanePlanVariance>): MutableList<BatchedLanePlanVariance> {
        val matched = filter(items) { candidate: BatchedLanePlanVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedLanePlanVariances(store: BatchedLanePlanVarianceStore, query: BatchedLanePlanVarianceQuery): MutableList<BatchedLanePlanVariance> {
    return query.apply(store.list())
}
