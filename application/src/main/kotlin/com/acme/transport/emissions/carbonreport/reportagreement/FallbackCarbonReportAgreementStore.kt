package com.acme.transport.emissions.carbonreport.reportagreement

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback carbon report agreements by code. */
interface FallbackCarbonReportAgreementStore {
    fun find(code: String): FallbackCarbonReportAgreement?
    fun save(item: FallbackCarbonReportAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackCarbonReportAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackCarbonReportAgreementStore(
    var items: MutableMap<String, FallbackCarbonReportAgreement>,
    var order: MutableList<String>,
) : FallbackCarbonReportAgreementStore {
    override fun find(code: String): FallbackCarbonReportAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackCarbonReportAgreement) {
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

    override fun list(): MutableList<FallbackCarbonReportAgreement> {
        val out = mutableListOf<FallbackCarbonReportAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackCarbonReportAgreementStore(): FallbackCarbonReportAgreementStore {
    return MemoryFallbackCarbonReportAgreementStore(
        mutableMapOf<String, FallbackCarbonReportAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackCarbonReportAgreementStore(
    var inner: FallbackCarbonReportAgreementStore,
    var cache: TtlCache<FallbackCarbonReportAgreement>,
    var hits: Int,
    var misses: Int,
) : FallbackCarbonReportAgreementStore {
    override fun find(code: String): FallbackCarbonReportAgreement? {
        val hit = (cache.get(code) as FallbackCarbonReportAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackCarbonReportAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackCarbonReportAgreement> = inner.list()
}

fun newCachedFallbackCarbonReportAgreementStore(inner: FallbackCarbonReportAgreementStore, ttl: Int, clock: Clock): FallbackCarbonReportAgreementStore {
    return CachedFallbackCarbonReportAgreementStore(
        inner = inner,
        cache = newTtlCache<FallbackCarbonReportAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback carbon report agreements. Empty fields match all. */
data class FallbackCarbonReportAgreementQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackCarbonReportAgreement): Boolean {
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

    fun apply(items: MutableList<FallbackCarbonReportAgreement>): MutableList<FallbackCarbonReportAgreement> {
        val matched = filter(items) { candidate: FallbackCarbonReportAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackCarbonReportAgreements(store: FallbackCarbonReportAgreementStore, query: FallbackCarbonReportAgreementQuery): MutableList<FallbackCarbonReportAgreement> {
    return query.apply(store.list())
}
