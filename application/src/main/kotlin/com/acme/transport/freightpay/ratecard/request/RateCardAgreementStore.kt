package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card agreements by code. */
interface RateCardAgreementStore {
    fun find(code: String): RateCardAgreement?
    fun save(item: RateCardAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardAgreementStore(
    var items: MutableMap<String, RateCardAgreement>,
    var order: MutableList<String>,
) : RateCardAgreementStore {
    override fun find(code: String): RateCardAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardAgreement) {
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

    override fun list(): MutableList<RateCardAgreement> {
        val out = mutableListOf<RateCardAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardAgreementStore(): RateCardAgreementStore {
    return MemoryRateCardAgreementStore(mutableMapOf<String, RateCardAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardAgreementStore(
    var inner: RateCardAgreementStore,
    var cache: TtlCache<RateCardAgreement>,
    var hits: Int,
    var misses: Int,
) : RateCardAgreementStore {
    override fun find(code: String): RateCardAgreement? {
        val hit = (cache.get(code) as RateCardAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardAgreement> = inner.list()
}

fun newCachedRateCardAgreementStore(inner: RateCardAgreementStore, ttl: Int, clock: Clock): RateCardAgreementStore {
    return CachedRateCardAgreementStore(
        inner = inner,
        cache = newTtlCache<RateCardAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card agreements. Empty fields match all. */
data class RateCardAgreementQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardAgreement>): MutableList<RateCardAgreement> {
        val matched = filter(items) { candidate: RateCardAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardAgreements(store: RateCardAgreementStore, query: RateCardAgreementQuery): MutableList<RateCardAgreement> {
    return query.apply(store.list())
}
