package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched tire thresholds by code. */
interface BatchedTireThresholdStore {
    fun find(code: String): BatchedTireThreshold?
    fun save(item: BatchedTireThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTireThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTireThresholdStore(
    var items: MutableMap<String, BatchedTireThreshold>,
    var order: MutableList<String>,
) : BatchedTireThresholdStore {
    override fun find(code: String): BatchedTireThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTireThreshold) {
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

    override fun list(): MutableList<BatchedTireThreshold> {
        val out = mutableListOf<BatchedTireThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTireThresholdStore(): BatchedTireThresholdStore {
    return MemoryBatchedTireThresholdStore(
        mutableMapOf<String, BatchedTireThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTireThresholdStore(
    var inner: BatchedTireThresholdStore,
    var cache: TtlCache<BatchedTireThreshold>,
    var hits: Int,
    var misses: Int,
) : BatchedTireThresholdStore {
    override fun find(code: String): BatchedTireThreshold? {
        val hit = (cache.get(code) as BatchedTireThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTireThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTireThreshold> = inner.list()
}

fun newCachedBatchedTireThresholdStore(inner: BatchedTireThresholdStore, ttl: Int, clock: Clock): BatchedTireThresholdStore {
    return CachedBatchedTireThresholdStore(
        inner = inner,
        cache = newTtlCache<BatchedTireThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched tire thresholds. Empty fields match all. */
data class BatchedTireThresholdQuery(
    var country: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTireThreshold): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedTireThreshold>): MutableList<BatchedTireThreshold> {
        val matched = filter(items) { candidate: BatchedTireThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTireThresholds(store: BatchedTireThresholdStore, query: BatchedTireThresholdQuery): MutableList<BatchedTireThreshold> {
    return query.apply(store.list())
}
