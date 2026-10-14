package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched duty period agreements by code. */
interface BatchedDutyPeriodAgreementStore {
    fun find(code: String): BatchedDutyPeriodAgreement?
    fun save(item: BatchedDutyPeriodAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDutyPeriodAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDutyPeriodAgreementStore(
    var items: MutableMap<String, BatchedDutyPeriodAgreement>,
    var order: MutableList<String>,
) : BatchedDutyPeriodAgreementStore {
    override fun find(code: String): BatchedDutyPeriodAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDutyPeriodAgreement) {
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

    override fun list(): MutableList<BatchedDutyPeriodAgreement> {
        val out = mutableListOf<BatchedDutyPeriodAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDutyPeriodAgreementStore(): BatchedDutyPeriodAgreementStore {
    return MemoryBatchedDutyPeriodAgreementStore(
        mutableMapOf<String, BatchedDutyPeriodAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDutyPeriodAgreementStore(
    var inner: BatchedDutyPeriodAgreementStore,
    var cache: TtlCache<BatchedDutyPeriodAgreement>,
    var hits: Int,
    var misses: Int,
) : BatchedDutyPeriodAgreementStore {
    override fun find(code: String): BatchedDutyPeriodAgreement? {
        val hit = (cache.get(code) as BatchedDutyPeriodAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDutyPeriodAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDutyPeriodAgreement> = inner.list()
}

fun newCachedBatchedDutyPeriodAgreementStore(inner: BatchedDutyPeriodAgreementStore, ttl: Int, clock: Clock): BatchedDutyPeriodAgreementStore {
    return CachedBatchedDutyPeriodAgreementStore(
        inner = inner,
        cache = newTtlCache<BatchedDutyPeriodAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched duty period agreements. Empty fields match all. */
data class BatchedDutyPeriodAgreementQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDutyPeriodAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDutyPeriodAgreement>): MutableList<BatchedDutyPeriodAgreement> {
        val matched = filter(items) { candidate: BatchedDutyPeriodAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDutyPeriodAgreements(store: BatchedDutyPeriodAgreementStore, query: BatchedDutyPeriodAgreementQuery): MutableList<BatchedDutyPeriodAgreement> {
    return query.apply(store.list())
}
