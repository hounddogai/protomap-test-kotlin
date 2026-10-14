package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled carbon report cycles by code. */
interface ScheduledCarbonReportCycleStore {
    fun find(code: String): ScheduledCarbonReportCycle?
    fun save(item: ScheduledCarbonReportCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledCarbonReportCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledCarbonReportCycleStore(
    var items: MutableMap<String, ScheduledCarbonReportCycle>,
    var order: MutableList<String>,
) : ScheduledCarbonReportCycleStore {
    override fun find(code: String): ScheduledCarbonReportCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledCarbonReportCycle) {
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

    override fun list(): MutableList<ScheduledCarbonReportCycle> {
        val out = mutableListOf<ScheduledCarbonReportCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledCarbonReportCycleStore(): ScheduledCarbonReportCycleStore {
    return MemoryScheduledCarbonReportCycleStore(
        mutableMapOf<String, ScheduledCarbonReportCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledCarbonReportCycleStore(
    var inner: ScheduledCarbonReportCycleStore,
    var cache: TtlCache<ScheduledCarbonReportCycle>,
    var hits: Int,
    var misses: Int,
) : ScheduledCarbonReportCycleStore {
    override fun find(code: String): ScheduledCarbonReportCycle? {
        val hit = (cache.get(code) as ScheduledCarbonReportCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledCarbonReportCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledCarbonReportCycle> = inner.list()
}

fun newCachedScheduledCarbonReportCycleStore(inner: ScheduledCarbonReportCycleStore, ttl: Int, clock: Clock): ScheduledCarbonReportCycleStore {
    return CachedScheduledCarbonReportCycleStore(
        inner = inner,
        cache = newTtlCache<ScheduledCarbonReportCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled carbon report cycles. Empty fields match all. */
data class ScheduledCarbonReportCycleQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledCarbonReportCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledCarbonReportCycle>): MutableList<ScheduledCarbonReportCycle> {
        val matched = filter(items) { candidate: ScheduledCarbonReportCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledCarbonReportCycles(store: ScheduledCarbonReportCycleStore, query: ScheduledCarbonReportCycleQuery): MutableList<ScheduledCarbonReportCycle> {
    return query.apply(store.list())
}
