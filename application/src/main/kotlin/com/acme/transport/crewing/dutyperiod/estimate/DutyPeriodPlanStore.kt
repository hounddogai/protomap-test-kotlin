package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period plans by code. */
interface DutyPeriodPlanStore {
    fun find(code: String): DutyPeriodPlan?
    fun save(item: DutyPeriodPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodPlanStore(
    var items: MutableMap<String, DutyPeriodPlan>,
    var order: MutableList<String>,
) : DutyPeriodPlanStore {
    override fun find(code: String): DutyPeriodPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodPlan) {
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

    override fun list(): MutableList<DutyPeriodPlan> {
        val out = mutableListOf<DutyPeriodPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodPlanStore(): DutyPeriodPlanStore {
    return MemoryDutyPeriodPlanStore(mutableMapOf<String, DutyPeriodPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodPlanStore(
    var inner: DutyPeriodPlanStore,
    var cache: TtlCache<DutyPeriodPlan>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodPlanStore {
    override fun find(code: String): DutyPeriodPlan? {
        val hit = (cache.get(code) as DutyPeriodPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodPlan> = inner.list()
}

fun newCachedDutyPeriodPlanStore(inner: DutyPeriodPlanStore, ttl: Int, clock: Clock): DutyPeriodPlanStore {
    return CachedDutyPeriodPlanStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period plans. Empty fields match all. */
data class DutyPeriodPlanQuery(
    var segment: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodPlan): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodPlan>): MutableList<DutyPeriodPlan> {
        val matched = filter(items) { candidate: DutyPeriodPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodPlans(store: DutyPeriodPlanStore, query: DutyPeriodPlanQuery): MutableList<DutyPeriodPlan> {
    return query.apply(store.list())
}
