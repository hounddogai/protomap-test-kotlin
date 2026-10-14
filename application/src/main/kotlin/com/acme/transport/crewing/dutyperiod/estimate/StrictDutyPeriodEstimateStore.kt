package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict duty period estimates by code. */
interface StrictDutyPeriodEstimateStore {
    fun find(code: String): StrictDutyPeriodEstimate?
    fun save(item: StrictDutyPeriodEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDutyPeriodEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDutyPeriodEstimateStore(
    var items: MutableMap<String, StrictDutyPeriodEstimate>,
    var order: MutableList<String>,
) : StrictDutyPeriodEstimateStore {
    override fun find(code: String): StrictDutyPeriodEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDutyPeriodEstimate) {
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

    override fun list(): MutableList<StrictDutyPeriodEstimate> {
        val out = mutableListOf<StrictDutyPeriodEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDutyPeriodEstimateStore(): StrictDutyPeriodEstimateStore {
    return MemoryStrictDutyPeriodEstimateStore(
        mutableMapOf<String, StrictDutyPeriodEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDutyPeriodEstimateStore(
    var inner: StrictDutyPeriodEstimateStore,
    var cache: TtlCache<StrictDutyPeriodEstimate>,
    var hits: Int,
    var misses: Int,
) : StrictDutyPeriodEstimateStore {
    override fun find(code: String): StrictDutyPeriodEstimate? {
        val hit = (cache.get(code) as StrictDutyPeriodEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDutyPeriodEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDutyPeriodEstimate> = inner.list()
}

fun newCachedStrictDutyPeriodEstimateStore(inner: StrictDutyPeriodEstimateStore, ttl: Int, clock: Clock): StrictDutyPeriodEstimateStore {
    return CachedStrictDutyPeriodEstimateStore(
        inner = inner,
        cache = newTtlCache<StrictDutyPeriodEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict duty period estimates. Empty fields match all. */
data class StrictDutyPeriodEstimateQuery(
    var country: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDutyPeriodEstimate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<StrictDutyPeriodEstimate>): MutableList<StrictDutyPeriodEstimate> {
        val matched = filter(items) { candidate: StrictDutyPeriodEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDutyPeriodEstimates(store: StrictDutyPeriodEstimateStore, query: StrictDutyPeriodEstimateQuery): MutableList<StrictDutyPeriodEstimate> {
    return query.apply(store.list())
}
