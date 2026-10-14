package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted carbon report ledgers by code. */
interface WeightedCarbonReportLedgerStore {
    fun find(code: String): WeightedCarbonReportLedger?
    fun save(item: WeightedCarbonReportLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCarbonReportLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCarbonReportLedgerStore(
    var items: MutableMap<String, WeightedCarbonReportLedger>,
    var order: MutableList<String>,
) : WeightedCarbonReportLedgerStore {
    override fun find(code: String): WeightedCarbonReportLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCarbonReportLedger) {
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

    override fun list(): MutableList<WeightedCarbonReportLedger> {
        val out = mutableListOf<WeightedCarbonReportLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCarbonReportLedgerStore(): WeightedCarbonReportLedgerStore {
    return MemoryWeightedCarbonReportLedgerStore(
        mutableMapOf<String, WeightedCarbonReportLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCarbonReportLedgerStore(
    var inner: WeightedCarbonReportLedgerStore,
    var cache: TtlCache<WeightedCarbonReportLedger>,
    var hits: Int,
    var misses: Int,
) : WeightedCarbonReportLedgerStore {
    override fun find(code: String): WeightedCarbonReportLedger? {
        val hit = (cache.get(code) as WeightedCarbonReportLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCarbonReportLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCarbonReportLedger> = inner.list()
}

fun newCachedWeightedCarbonReportLedgerStore(inner: WeightedCarbonReportLedgerStore, ttl: Int, clock: Clock): WeightedCarbonReportLedgerStore {
    return CachedWeightedCarbonReportLedgerStore(
        inner = inner,
        cache = newTtlCache<WeightedCarbonReportLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted carbon report ledgers. Empty fields match all. */
data class WeightedCarbonReportLedgerQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCarbonReportLedger): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<WeightedCarbonReportLedger>): MutableList<WeightedCarbonReportLedger> {
        val matched = filter(items) { candidate: WeightedCarbonReportLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCarbonReportLedgers(store: WeightedCarbonReportLedgerStore, query: WeightedCarbonReportLedgerQuery): MutableList<WeightedCarbonReportLedger> {
    return query.apply(store.list())
}
