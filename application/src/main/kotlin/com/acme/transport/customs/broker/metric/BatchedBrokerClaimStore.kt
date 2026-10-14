package com.acme.transport.customs.broker.metric

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched broker claims by code. */
interface BatchedBrokerClaimStore {
    fun find(code: String): BatchedBrokerClaim?
    fun save(item: BatchedBrokerClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedBrokerClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedBrokerClaimStore(
    var items: MutableMap<String, BatchedBrokerClaim>,
    var order: MutableList<String>,
) : BatchedBrokerClaimStore {
    override fun find(code: String): BatchedBrokerClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedBrokerClaim) {
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

    override fun list(): MutableList<BatchedBrokerClaim> {
        val out = mutableListOf<BatchedBrokerClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedBrokerClaimStore(): BatchedBrokerClaimStore {
    return MemoryBatchedBrokerClaimStore(mutableMapOf<String, BatchedBrokerClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedBrokerClaimStore(
    var inner: BatchedBrokerClaimStore,
    var cache: TtlCache<BatchedBrokerClaim>,
    var hits: Int,
    var misses: Int,
) : BatchedBrokerClaimStore {
    override fun find(code: String): BatchedBrokerClaim? {
        val hit = (cache.get(code) as BatchedBrokerClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedBrokerClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedBrokerClaim> = inner.list()
}

fun newCachedBatchedBrokerClaimStore(inner: BatchedBrokerClaimStore, ttl: Int, clock: Clock): BatchedBrokerClaimStore {
    return CachedBatchedBrokerClaimStore(
        inner = inner,
        cache = newTtlCache<BatchedBrokerClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched broker claims. Empty fields match all. */
data class BatchedBrokerClaimQuery(
    var region: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedBrokerClaim): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<BatchedBrokerClaim>): MutableList<BatchedBrokerClaim> {
        val matched = filter(items) { candidate: BatchedBrokerClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedBrokerClaims(store: BatchedBrokerClaimStore, query: BatchedBrokerClaimQuery): MutableList<BatchedBrokerClaim> {
    return query.apply(store.list())
}
