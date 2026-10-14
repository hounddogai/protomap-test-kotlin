package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched inspection claims by code. */
interface BatchedInspectionClaimStore {
    fun find(code: String): BatchedInspectionClaim?
    fun save(item: BatchedInspectionClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedInspectionClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedInspectionClaimStore(
    var items: MutableMap<String, BatchedInspectionClaim>,
    var order: MutableList<String>,
) : BatchedInspectionClaimStore {
    override fun find(code: String): BatchedInspectionClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedInspectionClaim) {
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

    override fun list(): MutableList<BatchedInspectionClaim> {
        val out = mutableListOf<BatchedInspectionClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedInspectionClaimStore(): BatchedInspectionClaimStore {
    return MemoryBatchedInspectionClaimStore(
        mutableMapOf<String, BatchedInspectionClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedInspectionClaimStore(
    var inner: BatchedInspectionClaimStore,
    var cache: TtlCache<BatchedInspectionClaim>,
    var hits: Int,
    var misses: Int,
) : BatchedInspectionClaimStore {
    override fun find(code: String): BatchedInspectionClaim? {
        val hit = (cache.get(code) as BatchedInspectionClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedInspectionClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedInspectionClaim> = inner.list()
}

fun newCachedBatchedInspectionClaimStore(inner: BatchedInspectionClaimStore, ttl: Int, clock: Clock): BatchedInspectionClaimStore {
    return CachedBatchedInspectionClaimStore(
        inner = inner,
        cache = newTtlCache<BatchedInspectionClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched inspection claims. Empty fields match all. */
data class BatchedInspectionClaimQuery(
    var country: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedInspectionClaim): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedInspectionClaim>): MutableList<BatchedInspectionClaim> {
        val matched = filter(items) { candidate: BatchedInspectionClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedInspectionClaims(store: BatchedInspectionClaimStore, query: BatchedInspectionClaimQuery): MutableList<BatchedInspectionClaim> {
    return query.apply(store.list())
}
