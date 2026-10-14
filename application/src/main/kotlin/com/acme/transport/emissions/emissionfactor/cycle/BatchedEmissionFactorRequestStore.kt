package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched emission factor requests by code. */
interface BatchedEmissionFactorRequestStore {
    fun find(code: String): BatchedEmissionFactorRequest?
    fun save(item: BatchedEmissionFactorRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedEmissionFactorRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedEmissionFactorRequestStore(
    var items: MutableMap<String, BatchedEmissionFactorRequest>,
    var order: MutableList<String>,
) : BatchedEmissionFactorRequestStore {
    override fun find(code: String): BatchedEmissionFactorRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedEmissionFactorRequest) {
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

    override fun list(): MutableList<BatchedEmissionFactorRequest> {
        val out = mutableListOf<BatchedEmissionFactorRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedEmissionFactorRequestStore(): BatchedEmissionFactorRequestStore {
    return MemoryBatchedEmissionFactorRequestStore(
        mutableMapOf<String, BatchedEmissionFactorRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedEmissionFactorRequestStore(
    var inner: BatchedEmissionFactorRequestStore,
    var cache: TtlCache<BatchedEmissionFactorRequest>,
    var hits: Int,
    var misses: Int,
) : BatchedEmissionFactorRequestStore {
    override fun find(code: String): BatchedEmissionFactorRequest? {
        val hit = (cache.get(code) as BatchedEmissionFactorRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedEmissionFactorRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedEmissionFactorRequest> = inner.list()
}

fun newCachedBatchedEmissionFactorRequestStore(inner: BatchedEmissionFactorRequestStore, ttl: Int, clock: Clock): BatchedEmissionFactorRequestStore {
    return CachedBatchedEmissionFactorRequestStore(
        inner = inner,
        cache = newTtlCache<BatchedEmissionFactorRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched emission factor requests. Empty fields match all. */
data class BatchedEmissionFactorRequestQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedEmissionFactorRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<BatchedEmissionFactorRequest>): MutableList<BatchedEmissionFactorRequest> {
        val matched = filter(items) { candidate: BatchedEmissionFactorRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedEmissionFactorRequests(store: BatchedEmissionFactorRequestStore, query: BatchedEmissionFactorRequestQuery): MutableList<BatchedEmissionFactorRequest> {
    return query.apply(store.list())
}
