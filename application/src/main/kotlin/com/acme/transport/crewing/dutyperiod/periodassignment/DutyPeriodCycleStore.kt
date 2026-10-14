package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period cycles by code. */
interface DutyPeriodCycleStore {
    fun find(code: String): DutyPeriodCycle?
    fun save(item: DutyPeriodCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodCycleStore(
    var items: MutableMap<String, DutyPeriodCycle>,
    var order: MutableList<String>,
) : DutyPeriodCycleStore {
    override fun find(code: String): DutyPeriodCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodCycle) {
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

    override fun list(): MutableList<DutyPeriodCycle> {
        val out = mutableListOf<DutyPeriodCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodCycleStore(): DutyPeriodCycleStore {
    return MemoryDutyPeriodCycleStore(mutableMapOf<String, DutyPeriodCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodCycleStore(
    var inner: DutyPeriodCycleStore,
    var cache: TtlCache<DutyPeriodCycle>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodCycleStore {
    override fun find(code: String): DutyPeriodCycle? {
        val hit = (cache.get(code) as DutyPeriodCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodCycle> = inner.list()
}

fun newCachedDutyPeriodCycleStore(inner: DutyPeriodCycleStore, ttl: Int, clock: Clock): DutyPeriodCycleStore {
    return CachedDutyPeriodCycleStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period cycles. Empty fields match all. */
data class DutyPeriodCycleQuery(
    var unit: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodCycle): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodCycle>): MutableList<DutyPeriodCycle> {
        val matched = filter(items) { candidate: DutyPeriodCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodCycles(store: DutyPeriodCycleStore, query: DutyPeriodCycleQuery): MutableList<DutyPeriodCycle> {
    return query.apply(store.list())
}
