package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched locker claims by code. */
interface BatchedLockerClaimStore {
    fun find(code: String): BatchedLockerClaim?
    fun save(item: BatchedLockerClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedLockerClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedLockerClaimStore(
    var items: MutableMap<String, BatchedLockerClaim>,
    var order: MutableList<String>,
) : BatchedLockerClaimStore {
    override fun find(code: String): BatchedLockerClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedLockerClaim) {
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

    override fun list(): MutableList<BatchedLockerClaim> {
        val out = mutableListOf<BatchedLockerClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedLockerClaimStore(): BatchedLockerClaimStore {
    return MemoryBatchedLockerClaimStore(mutableMapOf<String, BatchedLockerClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedLockerClaimStore(
    var inner: BatchedLockerClaimStore,
    var cache: TtlCache<BatchedLockerClaim>,
    var hits: Int,
    var misses: Int,
) : BatchedLockerClaimStore {
    override fun find(code: String): BatchedLockerClaim? {
        val hit = (cache.get(code) as BatchedLockerClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedLockerClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedLockerClaim> = inner.list()
}

fun newCachedBatchedLockerClaimStore(inner: BatchedLockerClaimStore, ttl: Int, clock: Clock): BatchedLockerClaimStore {
    return CachedBatchedLockerClaimStore(
        inner = inner,
        cache = newTtlCache<BatchedLockerClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched locker claims. Empty fields match all. */
data class BatchedLockerClaimQuery(
    var region: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedLockerClaim): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedLockerClaim>): MutableList<BatchedLockerClaim> {
        val matched = filter(items) { candidate: BatchedLockerClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedLockerClaims(store: BatchedLockerClaimStore, query: BatchedLockerClaimQuery): MutableList<BatchedLockerClaim> {
    return query.apply(store.list())
}
