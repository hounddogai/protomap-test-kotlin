package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional duty period estimates by code. */
interface RegionalDutyPeriodEstimateStore {
    fun find(code: String): RegionalDutyPeriodEstimate?
    fun save(item: RegionalDutyPeriodEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDutyPeriodEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDutyPeriodEstimateStore(
    var items: MutableMap<String, RegionalDutyPeriodEstimate>,
    var order: MutableList<String>,
) : RegionalDutyPeriodEstimateStore {
    override fun find(code: String): RegionalDutyPeriodEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDutyPeriodEstimate) {
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

    override fun list(): MutableList<RegionalDutyPeriodEstimate> {
        val out = mutableListOf<RegionalDutyPeriodEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDutyPeriodEstimateStore(): RegionalDutyPeriodEstimateStore {
    return MemoryRegionalDutyPeriodEstimateStore(
        mutableMapOf<String, RegionalDutyPeriodEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDutyPeriodEstimateStore(
    var inner: RegionalDutyPeriodEstimateStore,
    var cache: TtlCache<RegionalDutyPeriodEstimate>,
    var hits: Int,
    var misses: Int,
) : RegionalDutyPeriodEstimateStore {
    override fun find(code: String): RegionalDutyPeriodEstimate? {
        val hit = (cache.get(code) as RegionalDutyPeriodEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDutyPeriodEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDutyPeriodEstimate> = inner.list()
}

fun newCachedRegionalDutyPeriodEstimateStore(inner: RegionalDutyPeriodEstimateStore, ttl: Int, clock: Clock): RegionalDutyPeriodEstimateStore {
    return CachedRegionalDutyPeriodEstimateStore(
        inner = inner,
        cache = newTtlCache<RegionalDutyPeriodEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional duty period estimates. Empty fields match all. */
data class RegionalDutyPeriodEstimateQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDutyPeriodEstimate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDutyPeriodEstimate>): MutableList<RegionalDutyPeriodEstimate> {
        val matched = filter(items) { candidate: RegionalDutyPeriodEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDutyPeriodEstimates(store: RegionalDutyPeriodEstimateStore, query: RegionalDutyPeriodEstimateQuery): MutableList<RegionalDutyPeriodEstimate> {
    return query.apply(store.list())
}
