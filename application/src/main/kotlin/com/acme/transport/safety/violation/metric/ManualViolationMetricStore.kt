package com.acme.transport.safety.violation.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual violation metrics by code. */
interface ManualViolationMetricStore {
    fun find(code: String): ManualViolationMetric?
    fun save(item: ManualViolationMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualViolationMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualViolationMetricStore(
    var items: MutableMap<String, ManualViolationMetric>,
    var order: MutableList<String>,
) : ManualViolationMetricStore {
    override fun find(code: String): ManualViolationMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualViolationMetric) {
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

    override fun list(): MutableList<ManualViolationMetric> {
        val out = mutableListOf<ManualViolationMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualViolationMetricStore(): ManualViolationMetricStore {
    return MemoryManualViolationMetricStore(
        mutableMapOf<String, ManualViolationMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualViolationMetricStore(
    var inner: ManualViolationMetricStore,
    var cache: TtlCache<ManualViolationMetric>,
    var hits: Int,
    var misses: Int,
) : ManualViolationMetricStore {
    override fun find(code: String): ManualViolationMetric? {
        val hit = (cache.get(code) as ManualViolationMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualViolationMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualViolationMetric> = inner.list()
}

fun newCachedManualViolationMetricStore(inner: ManualViolationMetricStore, ttl: Int, clock: Clock): ManualViolationMetricStore {
    return CachedManualViolationMetricStore(
        inner = inner,
        cache = newTtlCache<ManualViolationMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual violation metrics. Empty fields match all. */
data class ManualViolationMetricQuery(
    var unit: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualViolationMetric): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ManualViolationMetric>): MutableList<ManualViolationMetric> {
        val matched = filter(items) { candidate: ManualViolationMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualViolationMetrics(store: ManualViolationMetricStore, query: ManualViolationMetricQuery): MutableList<ManualViolationMetric> {
    return query.apply(store.list())
}
