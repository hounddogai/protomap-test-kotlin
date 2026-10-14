package com.acme.transport.freightpay.ratecard.agreement

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending rate card agreements by code. */
interface PendingRateCardAgreementStore {
    fun find(code: String): PendingRateCardAgreement?
    fun save(item: PendingRateCardAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRateCardAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRateCardAgreementStore(
    var items: MutableMap<String, PendingRateCardAgreement>,
    var order: MutableList<String>,
) : PendingRateCardAgreementStore {
    override fun find(code: String): PendingRateCardAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRateCardAgreement) {
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

    override fun list(): MutableList<PendingRateCardAgreement> {
        val out = mutableListOf<PendingRateCardAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRateCardAgreementStore(): PendingRateCardAgreementStore {
    return MemoryPendingRateCardAgreementStore(
        mutableMapOf<String, PendingRateCardAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRateCardAgreementStore(
    var inner: PendingRateCardAgreementStore,
    var cache: TtlCache<PendingRateCardAgreement>,
    var hits: Int,
    var misses: Int,
) : PendingRateCardAgreementStore {
    override fun find(code: String): PendingRateCardAgreement? {
        val hit = (cache.get(code) as PendingRateCardAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRateCardAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRateCardAgreement> = inner.list()
}

fun newCachedPendingRateCardAgreementStore(inner: PendingRateCardAgreementStore, ttl: Int, clock: Clock): PendingRateCardAgreementStore {
    return CachedPendingRateCardAgreementStore(
        inner = inner,
        cache = newTtlCache<PendingRateCardAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending rate card agreements. Empty fields match all. */
data class PendingRateCardAgreementQuery(
    var category: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRateCardAgreement): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingRateCardAgreement>): MutableList<PendingRateCardAgreement> {
        val matched = filter(items) { candidate: PendingRateCardAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRateCardAgreements(store: PendingRateCardAgreementStore, query: PendingRateCardAgreementQuery): MutableList<PendingRateCardAgreement> {
    return query.apply(store.list())
}
