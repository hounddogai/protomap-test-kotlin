package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period estimates by code. */
interface DutyPeriodEstimateStore {
    fun find(code: String): DutyPeriodEstimate?
    fun save(item: DutyPeriodEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodEstimateStore(
    var items: MutableMap<String, DutyPeriodEstimate>,
    var order: MutableList<String>,
) : DutyPeriodEstimateStore {
    override fun find(code: String): DutyPeriodEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodEstimate) {
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

    override fun list(): MutableList<DutyPeriodEstimate> {
        val out = mutableListOf<DutyPeriodEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodEstimateStore(): DutyPeriodEstimateStore {
    return MemoryDutyPeriodEstimateStore(mutableMapOf<String, DutyPeriodEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodEstimateStore(
    var inner: DutyPeriodEstimateStore,
    var cache: TtlCache<DutyPeriodEstimate>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodEstimateStore {
    override fun find(code: String): DutyPeriodEstimate? {
        val hit = (cache.get(code) as DutyPeriodEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodEstimate> = inner.list()
}

fun newCachedDutyPeriodEstimateStore(inner: DutyPeriodEstimateStore, ttl: Int, clock: Clock): DutyPeriodEstimateStore {
    return CachedDutyPeriodEstimateStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period estimates. Empty fields match all. */
data class DutyPeriodEstimateQuery(
    var category: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodEstimate): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodEstimate>): MutableList<DutyPeriodEstimate> {
        val matched = filter(items) { candidate: DutyPeriodEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodEstimates(store: DutyPeriodEstimateStore, query: DutyPeriodEstimateQuery): MutableList<DutyPeriodEstimate> {
    return query.apply(store.list())
}
