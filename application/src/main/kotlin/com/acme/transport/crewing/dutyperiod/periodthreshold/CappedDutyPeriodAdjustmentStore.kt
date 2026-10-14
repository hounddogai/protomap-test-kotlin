package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped duty period adjustments by code. */
interface CappedDutyPeriodAdjustmentStore {
    fun find(code: String): CappedDutyPeriodAdjustment?
    fun save(item: CappedDutyPeriodAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDutyPeriodAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDutyPeriodAdjustmentStore(
    var items: MutableMap<String, CappedDutyPeriodAdjustment>,
    var order: MutableList<String>,
) : CappedDutyPeriodAdjustmentStore {
    override fun find(code: String): CappedDutyPeriodAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDutyPeriodAdjustment) {
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

    override fun list(): MutableList<CappedDutyPeriodAdjustment> {
        val out = mutableListOf<CappedDutyPeriodAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDutyPeriodAdjustmentStore(): CappedDutyPeriodAdjustmentStore {
    return MemoryCappedDutyPeriodAdjustmentStore(
        mutableMapOf<String, CappedDutyPeriodAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDutyPeriodAdjustmentStore(
    var inner: CappedDutyPeriodAdjustmentStore,
    var cache: TtlCache<CappedDutyPeriodAdjustment>,
    var hits: Int,
    var misses: Int,
) : CappedDutyPeriodAdjustmentStore {
    override fun find(code: String): CappedDutyPeriodAdjustment? {
        val hit = (cache.get(code) as CappedDutyPeriodAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDutyPeriodAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDutyPeriodAdjustment> = inner.list()
}

fun newCachedCappedDutyPeriodAdjustmentStore(inner: CappedDutyPeriodAdjustmentStore, ttl: Int, clock: Clock): CappedDutyPeriodAdjustmentStore {
    return CachedCappedDutyPeriodAdjustmentStore(
        inner = inner,
        cache = newTtlCache<CappedDutyPeriodAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped duty period adjustments. Empty fields match all. */
data class CappedDutyPeriodAdjustmentQuery(
    var channel: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDutyPeriodAdjustment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDutyPeriodAdjustment>): MutableList<CappedDutyPeriodAdjustment> {
        val matched = filter(items) { candidate: CappedDutyPeriodAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDutyPeriodAdjustments(store: CappedDutyPeriodAdjustmentStore, query: CappedDutyPeriodAdjustmentQuery): MutableList<CappedDutyPeriodAdjustment> {
    return query.apply(store.list())
}
