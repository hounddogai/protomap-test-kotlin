package com.acme.transport.network.servicearea.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area summaries by code. */
interface ServiceAreaSummaryStore {
    fun find(code: String): ServiceAreaSummary?
    fun save(item: ServiceAreaSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaSummaryStore(
    var items: MutableMap<String, ServiceAreaSummary>,
    var order: MutableList<String>,
) : ServiceAreaSummaryStore {
    override fun find(code: String): ServiceAreaSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaSummary) {
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

    override fun list(): MutableList<ServiceAreaSummary> {
        val out = mutableListOf<ServiceAreaSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaSummaryStore(): ServiceAreaSummaryStore {
    return MemoryServiceAreaSummaryStore(mutableMapOf<String, ServiceAreaSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaSummaryStore(
    var inner: ServiceAreaSummaryStore,
    var cache: TtlCache<ServiceAreaSummary>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaSummaryStore {
    override fun find(code: String): ServiceAreaSummary? {
        val hit = (cache.get(code) as ServiceAreaSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaSummary> = inner.list()
}

fun newCachedServiceAreaSummaryStore(inner: ServiceAreaSummaryStore, ttl: Int, clock: Clock): ServiceAreaSummaryStore {
    return CachedServiceAreaSummaryStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area summaries. Empty fields match all. */
data class ServiceAreaSummaryQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaSummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ServiceAreaSummary>): MutableList<ServiceAreaSummary> {
        val matched = filter(items) { candidate: ServiceAreaSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaSummaries(store: ServiceAreaSummaryStore, query: ServiceAreaSummaryQuery): MutableList<ServiceAreaSummary> {
    return query.apply(store.list())
}
