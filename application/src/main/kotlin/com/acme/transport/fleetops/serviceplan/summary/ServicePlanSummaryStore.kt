package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan summaries by code. */
interface ServicePlanSummaryStore {
    fun find(code: String): ServicePlanSummary?
    fun save(item: ServicePlanSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanSummaryStore(
    var items: MutableMap<String, ServicePlanSummary>,
    var order: MutableList<String>,
) : ServicePlanSummaryStore {
    override fun find(code: String): ServicePlanSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanSummary) {
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

    override fun list(): MutableList<ServicePlanSummary> {
        val out = mutableListOf<ServicePlanSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanSummaryStore(): ServicePlanSummaryStore {
    return MemoryServicePlanSummaryStore(mutableMapOf<String, ServicePlanSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanSummaryStore(
    var inner: ServicePlanSummaryStore,
    var cache: TtlCache<ServicePlanSummary>,
    var hits: Int,
    var misses: Int,
) : ServicePlanSummaryStore {
    override fun find(code: String): ServicePlanSummary? {
        val hit = (cache.get(code) as ServicePlanSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanSummary> = inner.list()
}

fun newCachedServicePlanSummaryStore(inner: ServicePlanSummaryStore, ttl: Int, clock: Clock): ServicePlanSummaryStore {
    return CachedServicePlanSummaryStore(
        inner = inner,
        cache = newTtlCache<ServicePlanSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan summaries. Empty fields match all. */
data class ServicePlanSummaryQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanSummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ServicePlanSummary>): MutableList<ServicePlanSummary> {
        val matched = filter(items) { candidate: ServicePlanSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanSummaries(store: ServicePlanSummaryStore, query: ServicePlanSummaryQuery): MutableList<ServicePlanSummary> {
    return query.apply(store.list())
}
