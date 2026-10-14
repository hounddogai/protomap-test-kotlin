package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched availability tiers by code. */
interface BatchedAvailabilityTierStore {
    fun find(code: String): BatchedAvailabilityTier?
    fun save(item: BatchedAvailabilityTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAvailabilityTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAvailabilityTierStore(
    var items: MutableMap<String, BatchedAvailabilityTier>,
    var order: MutableList<String>,
) : BatchedAvailabilityTierStore {
    override fun find(code: String): BatchedAvailabilityTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAvailabilityTier) {
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

    override fun list(): MutableList<BatchedAvailabilityTier> {
        val out = mutableListOf<BatchedAvailabilityTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAvailabilityTierStore(): BatchedAvailabilityTierStore {
    return MemoryBatchedAvailabilityTierStore(
        mutableMapOf<String, BatchedAvailabilityTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAvailabilityTierStore(
    var inner: BatchedAvailabilityTierStore,
    var cache: TtlCache<BatchedAvailabilityTier>,
    var hits: Int,
    var misses: Int,
) : BatchedAvailabilityTierStore {
    override fun find(code: String): BatchedAvailabilityTier? {
        val hit = (cache.get(code) as BatchedAvailabilityTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAvailabilityTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAvailabilityTier> = inner.list()
}

fun newCachedBatchedAvailabilityTierStore(inner: BatchedAvailabilityTierStore, ttl: Int, clock: Clock): BatchedAvailabilityTierStore {
    return CachedBatchedAvailabilityTierStore(
        inner = inner,
        cache = newTtlCache<BatchedAvailabilityTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched availability tiers. Empty fields match all. */
data class BatchedAvailabilityTierQuery(
    var segment: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAvailabilityTier): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<BatchedAvailabilityTier>): MutableList<BatchedAvailabilityTier> {
        val matched = filter(items) { candidate: BatchedAvailabilityTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAvailabilityTiers(store: BatchedAvailabilityTierStore, query: BatchedAvailabilityTierQuery): MutableList<BatchedAvailabilityTier> {
    return query.apply(store.list())
}
