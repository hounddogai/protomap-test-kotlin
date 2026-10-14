package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal carbon report ledgers by code. */
interface SeasonalCarbonReportLedgerStore {
    fun find(code: String): SeasonalCarbonReportLedger?
    fun save(item: SeasonalCarbonReportLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCarbonReportLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCarbonReportLedgerStore(
    var items: MutableMap<String, SeasonalCarbonReportLedger>,
    var order: MutableList<String>,
) : SeasonalCarbonReportLedgerStore {
    override fun find(code: String): SeasonalCarbonReportLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCarbonReportLedger) {
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

    override fun list(): MutableList<SeasonalCarbonReportLedger> {
        val out = mutableListOf<SeasonalCarbonReportLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCarbonReportLedgerStore(): SeasonalCarbonReportLedgerStore {
    return MemorySeasonalCarbonReportLedgerStore(
        mutableMapOf<String, SeasonalCarbonReportLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCarbonReportLedgerStore(
    var inner: SeasonalCarbonReportLedgerStore,
    var cache: TtlCache<SeasonalCarbonReportLedger>,
    var hits: Int,
    var misses: Int,
) : SeasonalCarbonReportLedgerStore {
    override fun find(code: String): SeasonalCarbonReportLedger? {
        val hit = (cache.get(code) as SeasonalCarbonReportLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCarbonReportLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCarbonReportLedger> = inner.list()
}

fun newCachedSeasonalCarbonReportLedgerStore(inner: SeasonalCarbonReportLedgerStore, ttl: Int, clock: Clock): SeasonalCarbonReportLedgerStore {
    return CachedSeasonalCarbonReportLedgerStore(
        inner = inner,
        cache = newTtlCache<SeasonalCarbonReportLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal carbon report ledgers. Empty fields match all. */
data class SeasonalCarbonReportLedgerQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCarbonReportLedger): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalCarbonReportLedger>): MutableList<SeasonalCarbonReportLedger> {
        val matched = filter(items) { candidate: SeasonalCarbonReportLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCarbonReportLedgers(store: SeasonalCarbonReportLedgerStore, query: SeasonalCarbonReportLedgerQuery): MutableList<SeasonalCarbonReportLedger> {
    return query.apply(store.list())
}
