package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched hub tiers by code. */
interface BatchedHubTierStore {
    fun find(code: String): BatchedHubTier?
    fun save(item: BatchedHubTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedHubTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedHubTierStore(
    var items: MutableMap<String, BatchedHubTier>,
    var order: MutableList<String>,
) : BatchedHubTierStore {
    override fun find(code: String): BatchedHubTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedHubTier) {
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

    override fun list(): MutableList<BatchedHubTier> {
        val out = mutableListOf<BatchedHubTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedHubTierStore(): BatchedHubTierStore {
    return MemoryBatchedHubTierStore(mutableMapOf<String, BatchedHubTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedHubTierStore(
    var inner: BatchedHubTierStore,
    var cache: TtlCache<BatchedHubTier>,
    var hits: Int,
    var misses: Int,
) : BatchedHubTierStore {
    override fun find(code: String): BatchedHubTier? {
        val hit = (cache.get(code) as BatchedHubTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedHubTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedHubTier> = inner.list()
}

fun newCachedBatchedHubTierStore(inner: BatchedHubTierStore, ttl: Int, clock: Clock): BatchedHubTierStore {
    return CachedBatchedHubTierStore(
        inner = inner,
        cache = newTtlCache<BatchedHubTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched hub tiers. Empty fields match all. */
data class BatchedHubTierQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedHubTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedHubTier>): MutableList<BatchedHubTier> {
        val matched = filter(items) { candidate: BatchedHubTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedHubTiers(store: BatchedHubTierStore, query: BatchedHubTierQuery): MutableList<BatchedHubTier> {
    return query.apply(store.list())
}
