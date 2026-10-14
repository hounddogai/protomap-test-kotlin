package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary carbon report agreements by code. */
interface PrimaryCarbonReportAgreementStore {
    fun find(code: String): PrimaryCarbonReportAgreement?
    fun save(item: PrimaryCarbonReportAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCarbonReportAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCarbonReportAgreementStore(
    var items: MutableMap<String, PrimaryCarbonReportAgreement>,
    var order: MutableList<String>,
) : PrimaryCarbonReportAgreementStore {
    override fun find(code: String): PrimaryCarbonReportAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCarbonReportAgreement) {
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

    override fun list(): MutableList<PrimaryCarbonReportAgreement> {
        val out = mutableListOf<PrimaryCarbonReportAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCarbonReportAgreementStore(): PrimaryCarbonReportAgreementStore {
    return MemoryPrimaryCarbonReportAgreementStore(
        mutableMapOf<String, PrimaryCarbonReportAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCarbonReportAgreementStore(
    var inner: PrimaryCarbonReportAgreementStore,
    var cache: TtlCache<PrimaryCarbonReportAgreement>,
    var hits: Int,
    var misses: Int,
) : PrimaryCarbonReportAgreementStore {
    override fun find(code: String): PrimaryCarbonReportAgreement? {
        val hit = (cache.get(code) as PrimaryCarbonReportAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCarbonReportAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCarbonReportAgreement> = inner.list()
}

fun newCachedPrimaryCarbonReportAgreementStore(inner: PrimaryCarbonReportAgreementStore, ttl: Int, clock: Clock): PrimaryCarbonReportAgreementStore {
    return CachedPrimaryCarbonReportAgreementStore(
        inner = inner,
        cache = newTtlCache<PrimaryCarbonReportAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary carbon report agreements. Empty fields match all. */
data class PrimaryCarbonReportAgreementQuery(
    var depot: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCarbonReportAgreement): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCarbonReportAgreement>): MutableList<PrimaryCarbonReportAgreement> {
        val matched = filter(items) { candidate: PrimaryCarbonReportAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCarbonReportAgreements(store: PrimaryCarbonReportAgreementStore, query: PrimaryCarbonReportAgreementQuery): MutableList<PrimaryCarbonReportAgreement> {
    return query.apply(store.list())
}
