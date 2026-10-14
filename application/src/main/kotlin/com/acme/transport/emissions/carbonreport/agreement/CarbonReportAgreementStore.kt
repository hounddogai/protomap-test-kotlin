package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report agreements by code. */
interface CarbonReportAgreementStore {
    fun find(code: String): CarbonReportAgreement?
    fun save(item: CarbonReportAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportAgreementStore(
    var items: MutableMap<String, CarbonReportAgreement>,
    var order: MutableList<String>,
) : CarbonReportAgreementStore {
    override fun find(code: String): CarbonReportAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportAgreement) {
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

    override fun list(): MutableList<CarbonReportAgreement> {
        val out = mutableListOf<CarbonReportAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportAgreementStore(): CarbonReportAgreementStore {
    return MemoryCarbonReportAgreementStore(
        mutableMapOf<String, CarbonReportAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportAgreementStore(
    var inner: CarbonReportAgreementStore,
    var cache: TtlCache<CarbonReportAgreement>,
    var hits: Int,
    var misses: Int,
) : CarbonReportAgreementStore {
    override fun find(code: String): CarbonReportAgreement? {
        val hit = (cache.get(code) as CarbonReportAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportAgreement> = inner.list()
}

fun newCachedCarbonReportAgreementStore(inner: CarbonReportAgreementStore, ttl: Int, clock: Clock): CarbonReportAgreementStore {
    return CachedCarbonReportAgreementStore(
        inner = inner,
        cache = newTtlCache<CarbonReportAgreement>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report agreements. Empty fields match all. */
data class CarbonReportAgreementQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportAgreement>): MutableList<CarbonReportAgreement> {
        val matched = filter(items) { candidate: CarbonReportAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportAgreements(store: CarbonReportAgreementStore, query: CarbonReportAgreementQuery): MutableList<CarbonReportAgreement> {
    return query.apply(store.list())
}
