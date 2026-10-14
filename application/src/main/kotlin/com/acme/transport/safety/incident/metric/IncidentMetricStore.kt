package com.acme.transport.safety.incident.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident metrics by code. */
interface IncidentMetricStore {
    fun find(code: String): IncidentMetric?
    fun save(item: IncidentMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentMetricStore(
    var items: MutableMap<String, IncidentMetric>,
    var order: MutableList<String>,
) : IncidentMetricStore {
    override fun find(code: String): IncidentMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentMetric) {
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

    override fun list(): MutableList<IncidentMetric> {
        val out = mutableListOf<IncidentMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentMetricStore(): IncidentMetricStore {
    return MemoryIncidentMetricStore(mutableMapOf<String, IncidentMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentMetricStore(
    var inner: IncidentMetricStore,
    var cache: TtlCache<IncidentMetric>,
    var hits: Int,
    var misses: Int,
) : IncidentMetricStore {
    override fun find(code: String): IncidentMetric? {
        val hit = (cache.get(code) as IncidentMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentMetric> = inner.list()
}

fun newCachedIncidentMetricStore(inner: IncidentMetricStore, ttl: Int, clock: Clock): IncidentMetricStore {
    return CachedIncidentMetricStore(
        inner = inner,
        cache = newTtlCache<IncidentMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident metrics. Empty fields match all. */
data class IncidentMetricQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentMetric>): MutableList<IncidentMetric> {
        val matched = filter(items) { candidate: IncidentMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentMetrics(store: IncidentMetricStore, query: IncidentMetricQuery): MutableList<IncidentMetric> {
    return query.apply(store.list())
}
