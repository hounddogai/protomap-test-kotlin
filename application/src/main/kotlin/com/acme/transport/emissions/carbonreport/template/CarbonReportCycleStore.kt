package com.acme.transport.emissions.carbonreport.template

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report cycles by code. */
interface CarbonReportCycleStore {
    fun find(code: String): CarbonReportCycle?
    fun save(item: CarbonReportCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportCycleStore(
    var items: MutableMap<String, CarbonReportCycle>,
    var order: MutableList<String>,
) : CarbonReportCycleStore {
    override fun find(code: String): CarbonReportCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportCycle) {
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

    override fun list(): MutableList<CarbonReportCycle> {
        val out = mutableListOf<CarbonReportCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportCycleStore(): CarbonReportCycleStore {
    return MemoryCarbonReportCycleStore(mutableMapOf<String, CarbonReportCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportCycleStore(
    var inner: CarbonReportCycleStore,
    var cache: TtlCache<CarbonReportCycle>,
    var hits: Int,
    var misses: Int,
) : CarbonReportCycleStore {
    override fun find(code: String): CarbonReportCycle? {
        val hit = (cache.get(code) as CarbonReportCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportCycle> = inner.list()
}

fun newCachedCarbonReportCycleStore(inner: CarbonReportCycleStore, ttl: Int, clock: Clock): CarbonReportCycleStore {
    return CachedCarbonReportCycleStore(
        inner = inner,
        cache = newTtlCache<CarbonReportCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report cycles. Empty fields match all. */
data class CarbonReportCycleQuery(
    var depot: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportCycle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportCycle>): MutableList<CarbonReportCycle> {
        val matched = filter(items) { candidate: CarbonReportCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportCycles(store: CarbonReportCycleStore, query: CarbonReportCycleQuery): MutableList<CarbonReportCycle> {
    return query.apply(store.list())
}
