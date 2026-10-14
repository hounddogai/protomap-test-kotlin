package com.acme.transport.safety.incident.incidenttarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global incident metrics by code. */
interface GlobalIncidentMetricStore {
    fun find(code: String): GlobalIncidentMetric?
    fun save(item: GlobalIncidentMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalIncidentMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalIncidentMetricStore(
    var items: MutableMap<String, GlobalIncidentMetric>,
    var order: MutableList<String>,
) : GlobalIncidentMetricStore {
    override fun find(code: String): GlobalIncidentMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalIncidentMetric) {
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

    override fun list(): MutableList<GlobalIncidentMetric> {
        val out = mutableListOf<GlobalIncidentMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalIncidentMetricStore(): GlobalIncidentMetricStore {
    return MemoryGlobalIncidentMetricStore(
        mutableMapOf<String, GlobalIncidentMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalIncidentMetricStore(
    var inner: GlobalIncidentMetricStore,
    var cache: TtlCache<GlobalIncidentMetric>,
    var hits: Int,
    var misses: Int,
) : GlobalIncidentMetricStore {
    override fun find(code: String): GlobalIncidentMetric? {
        val hit = (cache.get(code) as GlobalIncidentMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalIncidentMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalIncidentMetric> = inner.list()
}

fun newCachedGlobalIncidentMetricStore(inner: GlobalIncidentMetricStore, ttl: Int, clock: Clock): GlobalIncidentMetricStore {
    return CachedGlobalIncidentMetricStore(
        inner = inner,
        cache = newTtlCache<GlobalIncidentMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global incident metrics. Empty fields match all. */
data class GlobalIncidentMetricQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalIncidentMetric): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalIncidentMetric>): MutableList<GlobalIncidentMetric> {
        val matched = filter(items) { candidate: GlobalIncidentMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalIncidentMetrics(store: GlobalIncidentMetricStore, query: GlobalIncidentMetricQuery): MutableList<GlobalIncidentMetric> {
    return query.apply(store.list())
}
