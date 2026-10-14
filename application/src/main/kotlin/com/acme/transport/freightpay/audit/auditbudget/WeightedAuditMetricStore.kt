package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted audit metrics by code. */
interface WeightedAuditMetricStore {
    fun find(code: String): WeightedAuditMetric?
    fun save(item: WeightedAuditMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAuditMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAuditMetricStore(
    var items: MutableMap<String, WeightedAuditMetric>,
    var order: MutableList<String>,
) : WeightedAuditMetricStore {
    override fun find(code: String): WeightedAuditMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAuditMetric) {
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

    override fun list(): MutableList<WeightedAuditMetric> {
        val out = mutableListOf<WeightedAuditMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAuditMetricStore(): WeightedAuditMetricStore {
    return MemoryWeightedAuditMetricStore(mutableMapOf<String, WeightedAuditMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAuditMetricStore(
    var inner: WeightedAuditMetricStore,
    var cache: TtlCache<WeightedAuditMetric>,
    var hits: Int,
    var misses: Int,
) : WeightedAuditMetricStore {
    override fun find(code: String): WeightedAuditMetric? {
        val hit = (cache.get(code) as WeightedAuditMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAuditMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAuditMetric> = inner.list()
}

fun newCachedWeightedAuditMetricStore(inner: WeightedAuditMetricStore, ttl: Int, clock: Clock): WeightedAuditMetricStore {
    return CachedWeightedAuditMetricStore(
        inner = inner,
        cache = newTtlCache<WeightedAuditMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted audit metrics. Empty fields match all. */
data class WeightedAuditMetricQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAuditMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<WeightedAuditMetric>): MutableList<WeightedAuditMetric> {
        val matched = filter(items) { candidate: WeightedAuditMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAuditMetrics(store: WeightedAuditMetricStore, query: WeightedAuditMetricQuery): MutableList<WeightedAuditMetric> {
    return query.apply(store.list())
}
