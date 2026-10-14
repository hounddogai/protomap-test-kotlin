package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted duty period thresholds by code. */
interface WeightedDutyPeriodThresholdStore {
    fun find(code: String): WeightedDutyPeriodThreshold?
    fun save(item: WeightedDutyPeriodThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDutyPeriodThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDutyPeriodThresholdStore(
    var items: MutableMap<String, WeightedDutyPeriodThreshold>,
    var order: MutableList<String>,
) : WeightedDutyPeriodThresholdStore {
    override fun find(code: String): WeightedDutyPeriodThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDutyPeriodThreshold) {
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

    override fun list(): MutableList<WeightedDutyPeriodThreshold> {
        val out = mutableListOf<WeightedDutyPeriodThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDutyPeriodThresholdStore(): WeightedDutyPeriodThresholdStore {
    return MemoryWeightedDutyPeriodThresholdStore(
        mutableMapOf<String, WeightedDutyPeriodThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDutyPeriodThresholdStore(
    var inner: WeightedDutyPeriodThresholdStore,
    var cache: TtlCache<WeightedDutyPeriodThreshold>,
    var hits: Int,
    var misses: Int,
) : WeightedDutyPeriodThresholdStore {
    override fun find(code: String): WeightedDutyPeriodThreshold? {
        val hit = (cache.get(code) as WeightedDutyPeriodThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDutyPeriodThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDutyPeriodThreshold> = inner.list()
}

fun newCachedWeightedDutyPeriodThresholdStore(inner: WeightedDutyPeriodThresholdStore, ttl: Int, clock: Clock): WeightedDutyPeriodThresholdStore {
    return CachedWeightedDutyPeriodThresholdStore(
        inner = inner,
        cache = newTtlCache<WeightedDutyPeriodThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted duty period thresholds. Empty fields match all. */
data class WeightedDutyPeriodThresholdQuery(
    var category: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDutyPeriodThreshold): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDutyPeriodThreshold>): MutableList<WeightedDutyPeriodThreshold> {
        val matched = filter(items) { candidate: WeightedDutyPeriodThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDutyPeriodThresholds(store: WeightedDutyPeriodThresholdStore, query: WeightedDutyPeriodThresholdQuery): MutableList<WeightedDutyPeriodThreshold> {
    return query.apply(store.list())
}
