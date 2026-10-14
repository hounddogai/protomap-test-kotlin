package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched capacity agreements by code. */
interface BatchedCapacityAgreementStore {
    fun find(code: String): BatchedCapacityAgreement?
    fun save(item: BatchedCapacityAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCapacityAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCapacityAgreementStore(
    var items: MutableMap<String, BatchedCapacityAgreement>,
    var order: MutableList<String>,
) : BatchedCapacityAgreementStore {
    override fun find(code: String): BatchedCapacityAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCapacityAgreement) {
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

    override fun list(): MutableList<BatchedCapacityAgreement> {
        val out = mutableListOf<BatchedCapacityAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCapacityAgreementStore(): BatchedCapacityAgreementStore {
    return MemoryBatchedCapacityAgreementStore(
        mutableMapOf<String, BatchedCapacityAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCapacityAgreementStore(
    var inner: BatchedCapacityAgreementStore,
    var cache: TtlCache<BatchedCapacityAgreement>,
    var hits: Int,
    var misses: Int,
) : BatchedCapacityAgreementStore {
    override fun find(code: String): BatchedCapacityAgreement? {
        val hit = (cache.get(code) as BatchedCapacityAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCapacityAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCapacityAgreement> = inner.list()
}

fun newCachedBatchedCapacityAgreementStore(inner: BatchedCapacityAgreementStore, ttl: Int, clock: Clock): BatchedCapacityAgreementStore {
    return CachedBatchedCapacityAgreementStore(
        inner = inner,
        cache = newTtlCache<BatchedCapacityAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched capacity agreements. Empty fields match all. */
data class BatchedCapacityAgreementQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCapacityAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<BatchedCapacityAgreement>): MutableList<BatchedCapacityAgreement> {
        val matched = filter(items) { candidate: BatchedCapacityAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCapacityAgreements(store: BatchedCapacityAgreementStore, query: BatchedCapacityAgreementQuery): MutableList<BatchedCapacityAgreement> {
    return query.apply(store.list())
}
