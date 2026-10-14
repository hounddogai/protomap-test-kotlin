package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report ledgers by code. */
interface CarbonReportLedgerStore {
    fun find(code: String): CarbonReportLedger?
    fun save(item: CarbonReportLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportLedgerStore(
    var items: MutableMap<String, CarbonReportLedger>,
    var order: MutableList<String>,
) : CarbonReportLedgerStore {
    override fun find(code: String): CarbonReportLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportLedger) {
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

    override fun list(): MutableList<CarbonReportLedger> {
        val out = mutableListOf<CarbonReportLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportLedgerStore(): CarbonReportLedgerStore {
    return MemoryCarbonReportLedgerStore(mutableMapOf<String, CarbonReportLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportLedgerStore(
    var inner: CarbonReportLedgerStore,
    var cache: TtlCache<CarbonReportLedger>,
    var hits: Int,
    var misses: Int,
) : CarbonReportLedgerStore {
    override fun find(code: String): CarbonReportLedger? {
        val hit = (cache.get(code) as CarbonReportLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportLedger> = inner.list()
}

fun newCachedCarbonReportLedgerStore(inner: CarbonReportLedgerStore, ttl: Int, clock: Clock): CarbonReportLedgerStore {
    return CachedCarbonReportLedgerStore(
        inner = inner,
        cache = newTtlCache<CarbonReportLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report ledgers. Empty fields match all. */
data class CarbonReportLedgerQuery(
    var region: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportLedger): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportLedger>): MutableList<CarbonReportLedger> {
        val matched = filter(items) { candidate: CarbonReportLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportLedgers(store: CarbonReportLedgerStore, query: CarbonReportLedgerQuery): MutableList<CarbonReportLedger> {
    return query.apply(store.list())
}
