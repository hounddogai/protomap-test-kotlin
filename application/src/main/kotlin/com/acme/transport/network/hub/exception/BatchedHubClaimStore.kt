package com.acme.transport.network.hub.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched hub claims by code. */
interface BatchedHubClaimStore {
    fun find(code: String): BatchedHubClaim?
    fun save(item: BatchedHubClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedHubClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedHubClaimStore(
    var items: MutableMap<String, BatchedHubClaim>,
    var order: MutableList<String>,
) : BatchedHubClaimStore {
    override fun find(code: String): BatchedHubClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedHubClaim) {
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

    override fun list(): MutableList<BatchedHubClaim> {
        val out = mutableListOf<BatchedHubClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedHubClaimStore(): BatchedHubClaimStore {
    return MemoryBatchedHubClaimStore(mutableMapOf<String, BatchedHubClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedHubClaimStore(
    var inner: BatchedHubClaimStore,
    var cache: TtlCache<BatchedHubClaim>,
    var hits: Int,
    var misses: Int,
) : BatchedHubClaimStore {
    override fun find(code: String): BatchedHubClaim? {
        val hit = (cache.get(code) as BatchedHubClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedHubClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedHubClaim> = inner.list()
}

fun newCachedBatchedHubClaimStore(inner: BatchedHubClaimStore, ttl: Int, clock: Clock): BatchedHubClaimStore {
    return CachedBatchedHubClaimStore(
        inner = inner,
        cache = newTtlCache<BatchedHubClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched hub claims. Empty fields match all. */
data class BatchedHubClaimQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedHubClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<BatchedHubClaim>): MutableList<BatchedHubClaim> {
        val matched = filter(items) { candidate: BatchedHubClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedHubClaims(store: BatchedHubClaimStore, query: BatchedHubClaimQuery): MutableList<BatchedHubClaim> {
    return query.apply(store.list())
}
