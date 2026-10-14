package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary carbon report cycles by code. */
interface PrimaryCarbonReportCycleStore {
    fun find(code: String): PrimaryCarbonReportCycle?
    fun save(item: PrimaryCarbonReportCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCarbonReportCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCarbonReportCycleStore(
    var items: MutableMap<String, PrimaryCarbonReportCycle>,
    var order: MutableList<String>,
) : PrimaryCarbonReportCycleStore {
    override fun find(code: String): PrimaryCarbonReportCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCarbonReportCycle) {
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

    override fun list(): MutableList<PrimaryCarbonReportCycle> {
        val out = mutableListOf<PrimaryCarbonReportCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCarbonReportCycleStore(): PrimaryCarbonReportCycleStore {
    return MemoryPrimaryCarbonReportCycleStore(
        mutableMapOf<String, PrimaryCarbonReportCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCarbonReportCycleStore(
    var inner: PrimaryCarbonReportCycleStore,
    var cache: TtlCache<PrimaryCarbonReportCycle>,
    var hits: Int,
    var misses: Int,
) : PrimaryCarbonReportCycleStore {
    override fun find(code: String): PrimaryCarbonReportCycle? {
        val hit = (cache.get(code) as PrimaryCarbonReportCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCarbonReportCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCarbonReportCycle> = inner.list()
}

fun newCachedPrimaryCarbonReportCycleStore(inner: PrimaryCarbonReportCycleStore, ttl: Int, clock: Clock): PrimaryCarbonReportCycleStore {
    return CachedPrimaryCarbonReportCycleStore(
        inner = inner,
        cache = newTtlCache<PrimaryCarbonReportCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary carbon report cycles. Empty fields match all. */
data class PrimaryCarbonReportCycleQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCarbonReportCycle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCarbonReportCycle>): MutableList<PrimaryCarbonReportCycle> {
        val matched = filter(items) { candidate: PrimaryCarbonReportCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCarbonReportCycles(store: PrimaryCarbonReportCycleStore, query: PrimaryCarbonReportCycleQuery): MutableList<PrimaryCarbonReportCycle> {
    return query.apply(store.list())
}
