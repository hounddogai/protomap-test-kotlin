package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted appointment plans by code. */
interface WeightedAppointmentPlanStore {
    fun find(code: String): WeightedAppointmentPlan?
    fun save(item: WeightedAppointmentPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAppointmentPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAppointmentPlanStore(
    var items: MutableMap<String, WeightedAppointmentPlan>,
    var order: MutableList<String>,
) : WeightedAppointmentPlanStore {
    override fun find(code: String): WeightedAppointmentPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAppointmentPlan) {
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

    override fun list(): MutableList<WeightedAppointmentPlan> {
        val out = mutableListOf<WeightedAppointmentPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAppointmentPlanStore(): WeightedAppointmentPlanStore {
    return MemoryWeightedAppointmentPlanStore(
        mutableMapOf<String, WeightedAppointmentPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAppointmentPlanStore(
    var inner: WeightedAppointmentPlanStore,
    var cache: TtlCache<WeightedAppointmentPlan>,
    var hits: Int,
    var misses: Int,
) : WeightedAppointmentPlanStore {
    override fun find(code: String): WeightedAppointmentPlan? {
        val hit = (cache.get(code) as WeightedAppointmentPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAppointmentPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAppointmentPlan> = inner.list()
}

fun newCachedWeightedAppointmentPlanStore(inner: WeightedAppointmentPlanStore, ttl: Int, clock: Clock): WeightedAppointmentPlanStore {
    return CachedWeightedAppointmentPlanStore(
        inner = inner,
        cache = newTtlCache<WeightedAppointmentPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted appointment plans. Empty fields match all. */
data class WeightedAppointmentPlanQuery(
    var lane: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAppointmentPlan): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAppointmentPlan>): MutableList<WeightedAppointmentPlan> {
        val matched = filter(items) { candidate: WeightedAppointmentPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAppointmentPlans(store: WeightedAppointmentPlanStore, query: WeightedAppointmentPlanQuery): MutableList<WeightedAppointmentPlan> {
    return query.apply(store.list())
}
