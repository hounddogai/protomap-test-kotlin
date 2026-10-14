package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report requests by code. */
interface CarbonReportRequestStore {
    fun find(code: String): CarbonReportRequest?
    fun save(item: CarbonReportRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportRequestStore(
    var items: MutableMap<String, CarbonReportRequest>,
    var order: MutableList<String>,
) : CarbonReportRequestStore {
    override fun find(code: String): CarbonReportRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportRequest) {
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

    override fun list(): MutableList<CarbonReportRequest> {
        val out = mutableListOf<CarbonReportRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportRequestStore(): CarbonReportRequestStore {
    return MemoryCarbonReportRequestStore(mutableMapOf<String, CarbonReportRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportRequestStore(
    var inner: CarbonReportRequestStore,
    var cache: TtlCache<CarbonReportRequest>,
    var hits: Int,
    var misses: Int,
) : CarbonReportRequestStore {
    override fun find(code: String): CarbonReportRequest? {
        val hit = (cache.get(code) as CarbonReportRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportRequest> = inner.list()
}

fun newCachedCarbonReportRequestStore(inner: CarbonReportRequestStore, ttl: Int, clock: Clock): CarbonReportRequestStore {
    return CachedCarbonReportRequestStore(
        inner = inner,
        cache = newTtlCache<CarbonReportRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report requests. Empty fields match all. */
data class CarbonReportRequestQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportRequest>): MutableList<CarbonReportRequest> {
        val matched = filter(items) { candidate: CarbonReportRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportRequests(store: CarbonReportRequestStore, query: CarbonReportRequestQuery): MutableList<CarbonReportRequest> {
    return query.apply(store.list())
}
