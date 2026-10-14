package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return metrics by code. */
interface LeaseReturnMetricStore {
    fun find(code: String): LeaseReturnMetric?
    fun save(item: LeaseReturnMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnMetricStore(
    var items: MutableMap<String, LeaseReturnMetric>,
    var order: MutableList<String>,
) : LeaseReturnMetricStore {
    override fun find(code: String): LeaseReturnMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnMetric) {
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

    override fun list(): MutableList<LeaseReturnMetric> {
        val out = mutableListOf<LeaseReturnMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnMetricStore(): LeaseReturnMetricStore {
    return MemoryLeaseReturnMetricStore(mutableMapOf<String, LeaseReturnMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnMetricStore(
    var inner: LeaseReturnMetricStore,
    var cache: TtlCache<LeaseReturnMetric>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnMetricStore {
    override fun find(code: String): LeaseReturnMetric? {
        val hit = (cache.get(code) as LeaseReturnMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnMetric> = inner.list()
}

fun newCachedLeaseReturnMetricStore(inner: LeaseReturnMetricStore, ttl: Int, clock: Clock): LeaseReturnMetricStore {
    return CachedLeaseReturnMetricStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return metrics. Empty fields match all. */
data class LeaseReturnMetricQuery(
    var region: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnMetric): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnMetric>): MutableList<LeaseReturnMetric> {
        val matched = filter(items) { candidate: LeaseReturnMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnMetrics(store: LeaseReturnMetricStore, query: LeaseReturnMetricQuery): MutableList<LeaseReturnMetric> {
    return query.apply(store.list())
}
