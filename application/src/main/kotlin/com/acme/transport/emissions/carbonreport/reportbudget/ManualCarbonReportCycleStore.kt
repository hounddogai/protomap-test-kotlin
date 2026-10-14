package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual carbon report cycles by code. */
interface ManualCarbonReportCycleStore {
    fun find(code: String): ManualCarbonReportCycle?
    fun save(item: ManualCarbonReportCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCarbonReportCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCarbonReportCycleStore(
    var items: MutableMap<String, ManualCarbonReportCycle>,
    var order: MutableList<String>,
) : ManualCarbonReportCycleStore {
    override fun find(code: String): ManualCarbonReportCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCarbonReportCycle) {
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

    override fun list(): MutableList<ManualCarbonReportCycle> {
        val out = mutableListOf<ManualCarbonReportCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCarbonReportCycleStore(): ManualCarbonReportCycleStore {
    return MemoryManualCarbonReportCycleStore(
        mutableMapOf<String, ManualCarbonReportCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCarbonReportCycleStore(
    var inner: ManualCarbonReportCycleStore,
    var cache: TtlCache<ManualCarbonReportCycle>,
    var hits: Int,
    var misses: Int,
) : ManualCarbonReportCycleStore {
    override fun find(code: String): ManualCarbonReportCycle? {
        val hit = (cache.get(code) as ManualCarbonReportCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCarbonReportCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCarbonReportCycle> = inner.list()
}

fun newCachedManualCarbonReportCycleStore(inner: ManualCarbonReportCycleStore, ttl: Int, clock: Clock): ManualCarbonReportCycleStore {
    return CachedManualCarbonReportCycleStore(
        inner = inner,
        cache = newTtlCache<ManualCarbonReportCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual carbon report cycles. Empty fields match all. */
data class ManualCarbonReportCycleQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCarbonReportCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCarbonReportCycle>): MutableList<ManualCarbonReportCycle> {
        val matched = filter(items) { candidate: ManualCarbonReportCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCarbonReportCycles(store: ManualCarbonReportCycleStore, query: ManualCarbonReportCycleQuery): MutableList<ManualCarbonReportCycle> {
    return query.apply(store.list())
}
