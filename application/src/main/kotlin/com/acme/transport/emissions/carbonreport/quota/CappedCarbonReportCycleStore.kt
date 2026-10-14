package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped carbon report cycles by code. */
interface CappedCarbonReportCycleStore {
    fun find(code: String): CappedCarbonReportCycle?
    fun save(item: CappedCarbonReportCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCarbonReportCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCarbonReportCycleStore(
    var items: MutableMap<String, CappedCarbonReportCycle>,
    var order: MutableList<String>,
) : CappedCarbonReportCycleStore {
    override fun find(code: String): CappedCarbonReportCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCarbonReportCycle) {
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

    override fun list(): MutableList<CappedCarbonReportCycle> {
        val out = mutableListOf<CappedCarbonReportCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCarbonReportCycleStore(): CappedCarbonReportCycleStore {
    return MemoryCappedCarbonReportCycleStore(
        mutableMapOf<String, CappedCarbonReportCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCarbonReportCycleStore(
    var inner: CappedCarbonReportCycleStore,
    var cache: TtlCache<CappedCarbonReportCycle>,
    var hits: Int,
    var misses: Int,
) : CappedCarbonReportCycleStore {
    override fun find(code: String): CappedCarbonReportCycle? {
        val hit = (cache.get(code) as CappedCarbonReportCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCarbonReportCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCarbonReportCycle> = inner.list()
}

fun newCachedCappedCarbonReportCycleStore(inner: CappedCarbonReportCycleStore, ttl: Int, clock: Clock): CappedCarbonReportCycleStore {
    return CachedCappedCarbonReportCycleStore(
        inner = inner,
        cache = newTtlCache<CappedCarbonReportCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped carbon report cycles. Empty fields match all. */
data class CappedCarbonReportCycleQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCarbonReportCycle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedCarbonReportCycle>): MutableList<CappedCarbonReportCycle> {
        val matched = filter(items) { candidate: CappedCarbonReportCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCarbonReportCycles(store: CappedCarbonReportCycleStore, query: CappedCarbonReportCycleQuery): MutableList<CappedCarbonReportCycle> {
    return query.apply(store.list())
}
