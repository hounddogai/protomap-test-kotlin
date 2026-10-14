package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched fuel surcharge requests by code. */
interface BatchedFuelSurchargeRequestStore {
    fun find(code: String): BatchedFuelSurchargeRequest?
    fun save(item: BatchedFuelSurchargeRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedFuelSurchargeRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedFuelSurchargeRequestStore(
    var items: MutableMap<String, BatchedFuelSurchargeRequest>,
    var order: MutableList<String>,
) : BatchedFuelSurchargeRequestStore {
    override fun find(code: String): BatchedFuelSurchargeRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedFuelSurchargeRequest) {
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

    override fun list(): MutableList<BatchedFuelSurchargeRequest> {
        val out = mutableListOf<BatchedFuelSurchargeRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedFuelSurchargeRequestStore(): BatchedFuelSurchargeRequestStore {
    return MemoryBatchedFuelSurchargeRequestStore(
        mutableMapOf<String, BatchedFuelSurchargeRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedFuelSurchargeRequestStore(
    var inner: BatchedFuelSurchargeRequestStore,
    var cache: TtlCache<BatchedFuelSurchargeRequest>,
    var hits: Int,
    var misses: Int,
) : BatchedFuelSurchargeRequestStore {
    override fun find(code: String): BatchedFuelSurchargeRequest? {
        val hit = (cache.get(code) as BatchedFuelSurchargeRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedFuelSurchargeRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedFuelSurchargeRequest> = inner.list()
}

fun newCachedBatchedFuelSurchargeRequestStore(inner: BatchedFuelSurchargeRequestStore, ttl: Int, clock: Clock): BatchedFuelSurchargeRequestStore {
    return CachedBatchedFuelSurchargeRequestStore(
        inner = inner,
        cache = newTtlCache<BatchedFuelSurchargeRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched fuel surcharge requests. Empty fields match all. */
data class BatchedFuelSurchargeRequestQuery(
    var channel: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedFuelSurchargeRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedFuelSurchargeRequest>): MutableList<BatchedFuelSurchargeRequest> {
        val matched = filter(items) { candidate: BatchedFuelSurchargeRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedFuelSurchargeRequests(store: BatchedFuelSurchargeRequestStore, query: BatchedFuelSurchargeRequestQuery): MutableList<BatchedFuelSurchargeRequest> {
    return query.apply(store.list())
}
