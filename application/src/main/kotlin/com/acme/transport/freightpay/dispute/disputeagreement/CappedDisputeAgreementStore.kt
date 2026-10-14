package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped dispute agreements by code. */
interface CappedDisputeAgreementStore {
    fun find(code: String): CappedDisputeAgreement?
    fun save(item: CappedDisputeAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDisputeAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDisputeAgreementStore(
    var items: MutableMap<String, CappedDisputeAgreement>,
    var order: MutableList<String>,
) : CappedDisputeAgreementStore {
    override fun find(code: String): CappedDisputeAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDisputeAgreement) {
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

    override fun list(): MutableList<CappedDisputeAgreement> {
        val out = mutableListOf<CappedDisputeAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDisputeAgreementStore(): CappedDisputeAgreementStore {
    return MemoryCappedDisputeAgreementStore(
        mutableMapOf<String, CappedDisputeAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDisputeAgreementStore(
    var inner: CappedDisputeAgreementStore,
    var cache: TtlCache<CappedDisputeAgreement>,
    var hits: Int,
    var misses: Int,
) : CappedDisputeAgreementStore {
    override fun find(code: String): CappedDisputeAgreement? {
        val hit = (cache.get(code) as CappedDisputeAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDisputeAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDisputeAgreement> = inner.list()
}

fun newCachedCappedDisputeAgreementStore(inner: CappedDisputeAgreementStore, ttl: Int, clock: Clock): CappedDisputeAgreementStore {
    return CachedCappedDisputeAgreementStore(
        inner = inner,
        cache = newTtlCache<CappedDisputeAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dispute agreements. Empty fields match all. */
data class CappedDisputeAgreementQuery(
    var category: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDisputeAgreement): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDisputeAgreement>): MutableList<CappedDisputeAgreement> {
        val matched = filter(items) { candidate: CappedDisputeAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDisputeAgreements(store: CappedDisputeAgreementStore, query: CappedDisputeAgreementQuery): MutableList<CappedDisputeAgreement> {
    return query.apply(store.list())
}
