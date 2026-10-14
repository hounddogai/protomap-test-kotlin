package com.acme.transport.emissions.carbonreport.budget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional carbon report requests by code. */
interface RegionalCarbonReportRequestStore {
    fun find(code: String): RegionalCarbonReportRequest?
    fun save(item: RegionalCarbonReportRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCarbonReportRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCarbonReportRequestStore(
    var items: MutableMap<String, RegionalCarbonReportRequest>,
    var order: MutableList<String>,
) : RegionalCarbonReportRequestStore {
    override fun find(code: String): RegionalCarbonReportRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCarbonReportRequest) {
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

    override fun list(): MutableList<RegionalCarbonReportRequest> {
        val out = mutableListOf<RegionalCarbonReportRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCarbonReportRequestStore(): RegionalCarbonReportRequestStore {
    return MemoryRegionalCarbonReportRequestStore(
        mutableMapOf<String, RegionalCarbonReportRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCarbonReportRequestStore(
    var inner: RegionalCarbonReportRequestStore,
    var cache: TtlCache<RegionalCarbonReportRequest>,
    var hits: Int,
    var misses: Int,
) : RegionalCarbonReportRequestStore {
    override fun find(code: String): RegionalCarbonReportRequest? {
        val hit = (cache.get(code) as RegionalCarbonReportRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCarbonReportRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCarbonReportRequest> = inner.list()
}

fun newCachedRegionalCarbonReportRequestStore(inner: RegionalCarbonReportRequestStore, ttl: Int, clock: Clock): RegionalCarbonReportRequestStore {
    return CachedRegionalCarbonReportRequestStore(
        inner = inner,
        cache = newTtlCache<RegionalCarbonReportRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional carbon report requests. Empty fields match all. */
data class RegionalCarbonReportRequestQuery(
    var source: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCarbonReportRequest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCarbonReportRequest>): MutableList<RegionalCarbonReportRequest> {
        val matched = filter(items) { candidate: RegionalCarbonReportRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCarbonReportRequests(store: RegionalCarbonReportRequestStore, query: RegionalCarbonReportRequestQuery): MutableList<RegionalCarbonReportRequest> {
    return query.apply(store.list())
}
