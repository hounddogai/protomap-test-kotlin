package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched idling thresholds by code. */
interface BatchedIdlingThresholdStore {
    fun find(code: String): BatchedIdlingThreshold?
    fun save(item: BatchedIdlingThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedIdlingThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedIdlingThresholdStore(
    var items: MutableMap<String, BatchedIdlingThreshold>,
    var order: MutableList<String>,
) : BatchedIdlingThresholdStore {
    override fun find(code: String): BatchedIdlingThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedIdlingThreshold) {
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

    override fun list(): MutableList<BatchedIdlingThreshold> {
        val out = mutableListOf<BatchedIdlingThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedIdlingThresholdStore(): BatchedIdlingThresholdStore {
    return MemoryBatchedIdlingThresholdStore(
        mutableMapOf<String, BatchedIdlingThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedIdlingThresholdStore(
    var inner: BatchedIdlingThresholdStore,
    var cache: TtlCache<BatchedIdlingThreshold>,
    var hits: Int,
    var misses: Int,
) : BatchedIdlingThresholdStore {
    override fun find(code: String): BatchedIdlingThreshold? {
        val hit = (cache.get(code) as BatchedIdlingThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedIdlingThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedIdlingThreshold> = inner.list()
}

fun newCachedBatchedIdlingThresholdStore(inner: BatchedIdlingThresholdStore, ttl: Int, clock: Clock): BatchedIdlingThresholdStore {
    return CachedBatchedIdlingThresholdStore(
        inner = inner,
        cache = newTtlCache<BatchedIdlingThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched idling thresholds. Empty fields match all. */
data class BatchedIdlingThresholdQuery(
    var lane: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedIdlingThreshold): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedIdlingThreshold>): MutableList<BatchedIdlingThreshold> {
        val matched = filter(items) { candidate: BatchedIdlingThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedIdlingThresholds(store: BatchedIdlingThresholdStore, query: BatchedIdlingThresholdQuery): MutableList<BatchedIdlingThreshold> {
    return query.apply(store.list())
}
