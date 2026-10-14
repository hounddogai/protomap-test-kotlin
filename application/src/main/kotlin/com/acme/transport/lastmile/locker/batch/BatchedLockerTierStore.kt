package com.acme.transport.lastmile.locker.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched locker tiers by code. */
interface BatchedLockerTierStore {
    fun find(code: String): BatchedLockerTier?
    fun save(item: BatchedLockerTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedLockerTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedLockerTierStore(
    var items: MutableMap<String, BatchedLockerTier>,
    var order: MutableList<String>,
) : BatchedLockerTierStore {
    override fun find(code: String): BatchedLockerTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedLockerTier) {
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

    override fun list(): MutableList<BatchedLockerTier> {
        val out = mutableListOf<BatchedLockerTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedLockerTierStore(): BatchedLockerTierStore {
    return MemoryBatchedLockerTierStore(mutableMapOf<String, BatchedLockerTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedLockerTierStore(
    var inner: BatchedLockerTierStore,
    var cache: TtlCache<BatchedLockerTier>,
    var hits: Int,
    var misses: Int,
) : BatchedLockerTierStore {
    override fun find(code: String): BatchedLockerTier? {
        val hit = (cache.get(code) as BatchedLockerTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedLockerTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedLockerTier> = inner.list()
}

fun newCachedBatchedLockerTierStore(inner: BatchedLockerTierStore, ttl: Int, clock: Clock): BatchedLockerTierStore {
    return CachedBatchedLockerTierStore(
        inner = inner,
        cache = newTtlCache<BatchedLockerTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched locker tiers. Empty fields match all. */
data class BatchedLockerTierQuery(
    var segment: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedLockerTier): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedLockerTier>): MutableList<BatchedLockerTier> {
        val matched = filter(items) { candidate: BatchedLockerTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedLockerTiers(store: BatchedLockerTierStore, query: BatchedLockerTierQuery): MutableList<BatchedLockerTier> {
    return query.apply(store.list())
}
