package com.acme.transport.lastmile.attempt.attempttier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched attempt tiers by code. */
interface BatchedAttemptTierStore {
    fun find(code: String): BatchedAttemptTier?
    fun save(item: BatchedAttemptTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAttemptTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAttemptTierStore(
    var items: MutableMap<String, BatchedAttemptTier>,
    var order: MutableList<String>,
) : BatchedAttemptTierStore {
    override fun find(code: String): BatchedAttemptTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAttemptTier) {
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

    override fun list(): MutableList<BatchedAttemptTier> {
        val out = mutableListOf<BatchedAttemptTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAttemptTierStore(): BatchedAttemptTierStore {
    return MemoryBatchedAttemptTierStore(mutableMapOf<String, BatchedAttemptTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAttemptTierStore(
    var inner: BatchedAttemptTierStore,
    var cache: TtlCache<BatchedAttemptTier>,
    var hits: Int,
    var misses: Int,
) : BatchedAttemptTierStore {
    override fun find(code: String): BatchedAttemptTier? {
        val hit = (cache.get(code) as BatchedAttemptTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAttemptTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAttemptTier> = inner.list()
}

fun newCachedBatchedAttemptTierStore(inner: BatchedAttemptTierStore, ttl: Int, clock: Clock): BatchedAttemptTierStore {
    return CachedBatchedAttemptTierStore(
        inner = inner,
        cache = newTtlCache<BatchedAttemptTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched attempt tiers. Empty fields match all. */
data class BatchedAttemptTierQuery(
    var source: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAttemptTier): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAttemptTier>): MutableList<BatchedAttemptTier> {
        val matched = filter(items) { candidate: BatchedAttemptTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAttemptTiers(store: BatchedAttemptTierStore, query: BatchedAttemptTierQuery): MutableList<BatchedAttemptTier> {
    return query.apply(store.list())
}
