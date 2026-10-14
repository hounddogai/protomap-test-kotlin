package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict incident metrics by code. */
interface StrictIncidentMetricStore {
    fun find(code: String): StrictIncidentMetric?
    fun save(item: StrictIncidentMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictIncidentMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictIncidentMetricStore(
    var items: MutableMap<String, StrictIncidentMetric>,
    var order: MutableList<String>,
) : StrictIncidentMetricStore {
    override fun find(code: String): StrictIncidentMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictIncidentMetric) {
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

    override fun list(): MutableList<StrictIncidentMetric> {
        val out = mutableListOf<StrictIncidentMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictIncidentMetricStore(): StrictIncidentMetricStore {
    return MemoryStrictIncidentMetricStore(
        mutableMapOf<String, StrictIncidentMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictIncidentMetricStore(
    var inner: StrictIncidentMetricStore,
    var cache: TtlCache<StrictIncidentMetric>,
    var hits: Int,
    var misses: Int,
) : StrictIncidentMetricStore {
    override fun find(code: String): StrictIncidentMetric? {
        val hit = (cache.get(code) as StrictIncidentMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictIncidentMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictIncidentMetric> = inner.list()
}

fun newCachedStrictIncidentMetricStore(inner: StrictIncidentMetricStore, ttl: Int, clock: Clock): StrictIncidentMetricStore {
    return CachedStrictIncidentMetricStore(
        inner = inner,
        cache = newTtlCache<StrictIncidentMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict incident metrics. Empty fields match all. */
data class StrictIncidentMetricQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictIncidentMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictIncidentMetric>): MutableList<StrictIncidentMetric> {
        val matched = filter(items) { candidate: StrictIncidentMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictIncidentMetrics(store: StrictIncidentMetricStore, query: StrictIncidentMetricQuery): MutableList<StrictIncidentMetric> {
    return query.apply(store.list())
}
