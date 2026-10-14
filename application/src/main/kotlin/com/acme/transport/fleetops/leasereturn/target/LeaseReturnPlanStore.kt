package com.acme.transport.fleetops.leasereturn.target

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return plans by code. */
interface LeaseReturnPlanStore {
    fun find(code: String): LeaseReturnPlan?
    fun save(item: LeaseReturnPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnPlanStore(
    var items: MutableMap<String, LeaseReturnPlan>,
    var order: MutableList<String>,
) : LeaseReturnPlanStore {
    override fun find(code: String): LeaseReturnPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnPlan) {
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

    override fun list(): MutableList<LeaseReturnPlan> {
        val out = mutableListOf<LeaseReturnPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnPlanStore(): LeaseReturnPlanStore {
    return MemoryLeaseReturnPlanStore(mutableMapOf<String, LeaseReturnPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnPlanStore(
    var inner: LeaseReturnPlanStore,
    var cache: TtlCache<LeaseReturnPlan>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnPlanStore {
    override fun find(code: String): LeaseReturnPlan? {
        val hit = (cache.get(code) as LeaseReturnPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnPlan> = inner.list()
}

fun newCachedLeaseReturnPlanStore(inner: LeaseReturnPlanStore, ttl: Int, clock: Clock): LeaseReturnPlanStore {
    return CachedLeaseReturnPlanStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return plans. Empty fields match all. */
data class LeaseReturnPlanQuery(
    var unit: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnPlan): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnPlan>): MutableList<LeaseReturnPlan> {
        val matched = filter(items) { candidate: LeaseReturnPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnPlans(store: LeaseReturnPlanStore, query: LeaseReturnPlanQuery): MutableList<LeaseReturnPlan> {
    return query.apply(store.list())
}
