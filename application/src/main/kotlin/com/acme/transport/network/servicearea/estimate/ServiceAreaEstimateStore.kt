package com.acme.transport.network.servicearea.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area estimates by code. */
interface ServiceAreaEstimateStore {
    fun find(code: String): ServiceAreaEstimate?
    fun save(item: ServiceAreaEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaEstimateStore(
    var items: MutableMap<String, ServiceAreaEstimate>,
    var order: MutableList<String>,
) : ServiceAreaEstimateStore {
    override fun find(code: String): ServiceAreaEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaEstimate) {
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

    override fun list(): MutableList<ServiceAreaEstimate> {
        val out = mutableListOf<ServiceAreaEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaEstimateStore(): ServiceAreaEstimateStore {
    return MemoryServiceAreaEstimateStore(mutableMapOf<String, ServiceAreaEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaEstimateStore(
    var inner: ServiceAreaEstimateStore,
    var cache: TtlCache<ServiceAreaEstimate>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaEstimateStore {
    override fun find(code: String): ServiceAreaEstimate? {
        val hit = (cache.get(code) as ServiceAreaEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaEstimate> = inner.list()
}

fun newCachedServiceAreaEstimateStore(inner: ServiceAreaEstimateStore, ttl: Int, clock: Clock): ServiceAreaEstimateStore {
    return CachedServiceAreaEstimateStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area estimates. Empty fields match all. */
data class ServiceAreaEstimateQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaEstimate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaEstimate>): MutableList<ServiceAreaEstimate> {
        val matched = filter(items) { candidate: ServiceAreaEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaEstimates(store: ServiceAreaEstimateStore, query: ServiceAreaEstimateQuery): MutableList<ServiceAreaEstimate> {
    return query.apply(store.list())
}
