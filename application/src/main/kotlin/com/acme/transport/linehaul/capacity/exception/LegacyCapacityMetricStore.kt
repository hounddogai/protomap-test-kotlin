package com.acme.transport.linehaul.capacity.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy capacity metrics by code. */
interface LegacyCapacityMetricStore {
    fun find(code: String): LegacyCapacityMetric?
    fun save(item: LegacyCapacityMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCapacityMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCapacityMetricStore(
    var items: MutableMap<String, LegacyCapacityMetric>,
    var order: MutableList<String>,
) : LegacyCapacityMetricStore {
    override fun find(code: String): LegacyCapacityMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCapacityMetric) {
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

    override fun list(): MutableList<LegacyCapacityMetric> {
        val out = mutableListOf<LegacyCapacityMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCapacityMetricStore(): LegacyCapacityMetricStore {
    return MemoryLegacyCapacityMetricStore(
        mutableMapOf<String, LegacyCapacityMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCapacityMetricStore(
    var inner: LegacyCapacityMetricStore,
    var cache: TtlCache<LegacyCapacityMetric>,
    var hits: Int,
    var misses: Int,
) : LegacyCapacityMetricStore {
    override fun find(code: String): LegacyCapacityMetric? {
        val hit = (cache.get(code) as LegacyCapacityMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCapacityMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCapacityMetric> = inner.list()
}

fun newCachedLegacyCapacityMetricStore(inner: LegacyCapacityMetricStore, ttl: Int, clock: Clock): LegacyCapacityMetricStore {
    return CachedLegacyCapacityMetricStore(
        inner = inner,
        cache = newTtlCache<LegacyCapacityMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy capacity metrics. Empty fields match all. */
data class LegacyCapacityMetricQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCapacityMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyCapacityMetric>): MutableList<LegacyCapacityMetric> {
        val matched = filter(items) { candidate: LegacyCapacityMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCapacityMetrics(store: LegacyCapacityMetricStore, query: LegacyCapacityMetricQuery): MutableList<LegacyCapacityMetric> {
    return query.apply(store.list())
}
