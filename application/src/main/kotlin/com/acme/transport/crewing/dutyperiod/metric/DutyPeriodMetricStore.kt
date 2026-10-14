package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period metrics by code. */
interface DutyPeriodMetricStore {
    fun find(code: String): DutyPeriodMetric?
    fun save(item: DutyPeriodMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodMetricStore(
    var items: MutableMap<String, DutyPeriodMetric>,
    var order: MutableList<String>,
) : DutyPeriodMetricStore {
    override fun find(code: String): DutyPeriodMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodMetric) {
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

    override fun list(): MutableList<DutyPeriodMetric> {
        val out = mutableListOf<DutyPeriodMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodMetricStore(): DutyPeriodMetricStore {
    return MemoryDutyPeriodMetricStore(mutableMapOf<String, DutyPeriodMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodMetricStore(
    var inner: DutyPeriodMetricStore,
    var cache: TtlCache<DutyPeriodMetric>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodMetricStore {
    override fun find(code: String): DutyPeriodMetric? {
        val hit = (cache.get(code) as DutyPeriodMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodMetric> = inner.list()
}

fun newCachedDutyPeriodMetricStore(inner: DutyPeriodMetricStore, ttl: Int, clock: Clock): DutyPeriodMetricStore {
    return CachedDutyPeriodMetricStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period metrics. Empty fields match all. */
data class DutyPeriodMetricQuery(
    var lane: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodMetric): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodMetric>): MutableList<DutyPeriodMetric> {
        val matched = filter(items) { candidate: DutyPeriodMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodMetrics(store: DutyPeriodMetricStore, query: DutyPeriodMetricQuery): MutableList<DutyPeriodMetric> {
    return query.apply(store.list())
}
