package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal carbon report agreements by code. */
interface SeasonalCarbonReportAgreementStore {
    fun find(code: String): SeasonalCarbonReportAgreement?
    fun save(item: SeasonalCarbonReportAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCarbonReportAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCarbonReportAgreementStore(
    var items: MutableMap<String, SeasonalCarbonReportAgreement>,
    var order: MutableList<String>,
) : SeasonalCarbonReportAgreementStore {
    override fun find(code: String): SeasonalCarbonReportAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCarbonReportAgreement) {
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

    override fun list(): MutableList<SeasonalCarbonReportAgreement> {
        val out = mutableListOf<SeasonalCarbonReportAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCarbonReportAgreementStore(): SeasonalCarbonReportAgreementStore {
    return MemorySeasonalCarbonReportAgreementStore(
        mutableMapOf<String, SeasonalCarbonReportAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCarbonReportAgreementStore(
    var inner: SeasonalCarbonReportAgreementStore,
    var cache: TtlCache<SeasonalCarbonReportAgreement>,
    var hits: Int,
    var misses: Int,
) : SeasonalCarbonReportAgreementStore {
    override fun find(code: String): SeasonalCarbonReportAgreement? {
        val hit = (cache.get(code) as SeasonalCarbonReportAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCarbonReportAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCarbonReportAgreement> = inner.list()
}

fun newCachedSeasonalCarbonReportAgreementStore(inner: SeasonalCarbonReportAgreementStore, ttl: Int, clock: Clock): SeasonalCarbonReportAgreementStore {
    return CachedSeasonalCarbonReportAgreementStore(
        inner = inner,
        cache = newTtlCache<SeasonalCarbonReportAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal carbon report agreements. Empty fields match all. */
data class SeasonalCarbonReportAgreementQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCarbonReportAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalCarbonReportAgreement>): MutableList<SeasonalCarbonReportAgreement> {
        val matched = filter(items) { candidate: SeasonalCarbonReportAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCarbonReportAgreements(store: SeasonalCarbonReportAgreementStore, query: SeasonalCarbonReportAgreementQuery): MutableList<SeasonalCarbonReportAgreement> {
    return query.apply(store.list())
}
