package com.acme.transport.crewing.availability.exception

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched availability agreements by code. */
interface BatchedAvailabilityAgreementStore {
    fun find(code: String): BatchedAvailabilityAgreement?
    fun save(item: BatchedAvailabilityAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAvailabilityAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAvailabilityAgreementStore(
    var items: MutableMap<String, BatchedAvailabilityAgreement>,
    var order: MutableList<String>,
) : BatchedAvailabilityAgreementStore {
    override fun find(code: String): BatchedAvailabilityAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAvailabilityAgreement) {
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

    override fun list(): MutableList<BatchedAvailabilityAgreement> {
        val out = mutableListOf<BatchedAvailabilityAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAvailabilityAgreementStore(): BatchedAvailabilityAgreementStore {
    return MemoryBatchedAvailabilityAgreementStore(
        mutableMapOf<String, BatchedAvailabilityAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAvailabilityAgreementStore(
    var inner: BatchedAvailabilityAgreementStore,
    var cache: TtlCache<BatchedAvailabilityAgreement>,
    var hits: Int,
    var misses: Int,
) : BatchedAvailabilityAgreementStore {
    override fun find(code: String): BatchedAvailabilityAgreement? {
        val hit = (cache.get(code) as BatchedAvailabilityAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAvailabilityAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAvailabilityAgreement> = inner.list()
}

fun newCachedBatchedAvailabilityAgreementStore(inner: BatchedAvailabilityAgreementStore, ttl: Int, clock: Clock): BatchedAvailabilityAgreementStore {
    return CachedBatchedAvailabilityAgreementStore(
        inner = inner,
        cache = newTtlCache<BatchedAvailabilityAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched availability agreements. Empty fields match all. */
data class BatchedAvailabilityAgreementQuery(
    var depot: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAvailabilityAgreement): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<BatchedAvailabilityAgreement>): MutableList<BatchedAvailabilityAgreement> {
        val matched = filter(items) { candidate: BatchedAvailabilityAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAvailabilityAgreements(store: BatchedAvailabilityAgreementStore, query: BatchedAvailabilityAgreementQuery): MutableList<BatchedAvailabilityAgreement> {
    return query.apply(store.list())
}
