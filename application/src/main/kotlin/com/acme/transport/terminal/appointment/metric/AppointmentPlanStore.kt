package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment plans by code. */
interface AppointmentPlanStore {
    fun find(code: String): AppointmentPlan?
    fun save(item: AppointmentPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentPlanStore(
    var items: MutableMap<String, AppointmentPlan>,
    var order: MutableList<String>,
) : AppointmentPlanStore {
    override fun find(code: String): AppointmentPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentPlan) {
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

    override fun list(): MutableList<AppointmentPlan> {
        val out = mutableListOf<AppointmentPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentPlanStore(): AppointmentPlanStore {
    return MemoryAppointmentPlanStore(mutableMapOf<String, AppointmentPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentPlanStore(
    var inner: AppointmentPlanStore,
    var cache: TtlCache<AppointmentPlan>,
    var hits: Int,
    var misses: Int,
) : AppointmentPlanStore {
    override fun find(code: String): AppointmentPlan? {
        val hit = (cache.get(code) as AppointmentPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentPlan> = inner.list()
}

fun newCachedAppointmentPlanStore(inner: AppointmentPlanStore, ttl: Int, clock: Clock): AppointmentPlanStore {
    return CachedAppointmentPlanStore(
        inner = inner,
        cache = newTtlCache<AppointmentPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment plans. Empty fields match all. */
data class AppointmentPlanQuery(
    var source: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentPlan): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<AppointmentPlan>): MutableList<AppointmentPlan> {
        val matched = filter(items) { candidate: AppointmentPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentPlans(store: AppointmentPlanStore, query: AppointmentPlanQuery): MutableList<AppointmentPlan> {
    return query.apply(store.list())
}
