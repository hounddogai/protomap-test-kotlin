package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period adjustments by code. */
interface DutyPeriodAdjustmentStore {
    fun find(code: String): DutyPeriodAdjustment?
    fun save(item: DutyPeriodAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodAdjustmentStore(
    var items: MutableMap<String, DutyPeriodAdjustment>,
    var order: MutableList<String>,
) : DutyPeriodAdjustmentStore {
    override fun find(code: String): DutyPeriodAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodAdjustment) {
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

    override fun list(): MutableList<DutyPeriodAdjustment> {
        val out = mutableListOf<DutyPeriodAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodAdjustmentStore(): DutyPeriodAdjustmentStore {
    return MemoryDutyPeriodAdjustmentStore(
        mutableMapOf<String, DutyPeriodAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodAdjustmentStore(
    var inner: DutyPeriodAdjustmentStore,
    var cache: TtlCache<DutyPeriodAdjustment>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodAdjustmentStore {
    override fun find(code: String): DutyPeriodAdjustment? {
        val hit = (cache.get(code) as DutyPeriodAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodAdjustment> = inner.list()
}

fun newCachedDutyPeriodAdjustmentStore(inner: DutyPeriodAdjustmentStore, ttl: Int, clock: Clock): DutyPeriodAdjustmentStore {
    return CachedDutyPeriodAdjustmentStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period adjustments. Empty fields match all. */
data class DutyPeriodAdjustmentQuery(
    var channel: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodAdjustment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodAdjustment>): MutableList<DutyPeriodAdjustment> {
        val matched = filter(items) { candidate: DutyPeriodAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodAdjustments(store: DutyPeriodAdjustmentStore, query: DutyPeriodAdjustmentQuery): MutableList<DutyPeriodAdjustment> {
    return query.apply(store.list())
}
