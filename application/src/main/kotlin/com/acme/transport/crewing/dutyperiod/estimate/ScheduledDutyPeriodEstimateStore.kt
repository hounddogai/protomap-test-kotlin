package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled duty period estimates by code. */
interface ScheduledDutyPeriodEstimateStore {
    fun find(code: String): ScheduledDutyPeriodEstimate?
    fun save(item: ScheduledDutyPeriodEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDutyPeriodEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDutyPeriodEstimateStore(
    var items: MutableMap<String, ScheduledDutyPeriodEstimate>,
    var order: MutableList<String>,
) : ScheduledDutyPeriodEstimateStore {
    override fun find(code: String): ScheduledDutyPeriodEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDutyPeriodEstimate) {
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

    override fun list(): MutableList<ScheduledDutyPeriodEstimate> {
        val out = mutableListOf<ScheduledDutyPeriodEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDutyPeriodEstimateStore(): ScheduledDutyPeriodEstimateStore {
    return MemoryScheduledDutyPeriodEstimateStore(
        mutableMapOf<String, ScheduledDutyPeriodEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDutyPeriodEstimateStore(
    var inner: ScheduledDutyPeriodEstimateStore,
    var cache: TtlCache<ScheduledDutyPeriodEstimate>,
    var hits: Int,
    var misses: Int,
) : ScheduledDutyPeriodEstimateStore {
    override fun find(code: String): ScheduledDutyPeriodEstimate? {
        val hit = (cache.get(code) as ScheduledDutyPeriodEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDutyPeriodEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDutyPeriodEstimate> = inner.list()
}

fun newCachedScheduledDutyPeriodEstimateStore(inner: ScheduledDutyPeriodEstimateStore, ttl: Int, clock: Clock): ScheduledDutyPeriodEstimateStore {
    return CachedScheduledDutyPeriodEstimateStore(
        inner = inner,
        cache = newTtlCache<ScheduledDutyPeriodEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled duty period estimates. Empty fields match all. */
data class ScheduledDutyPeriodEstimateQuery(
    var locale: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDutyPeriodEstimate): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDutyPeriodEstimate>): MutableList<ScheduledDutyPeriodEstimate> {
        val matched = filter(items) { candidate: ScheduledDutyPeriodEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDutyPeriodEstimates(store: ScheduledDutyPeriodEstimateStore, query: ScheduledDutyPeriodEstimateQuery): MutableList<ScheduledDutyPeriodEstimate> {
    return query.apply(store.list())
}
