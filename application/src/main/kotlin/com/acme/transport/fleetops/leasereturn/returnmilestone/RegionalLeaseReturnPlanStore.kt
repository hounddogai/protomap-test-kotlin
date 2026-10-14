package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional lease return plans by code. */
interface RegionalLeaseReturnPlanStore {
    fun find(code: String): RegionalLeaseReturnPlan?
    fun save(item: RegionalLeaseReturnPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalLeaseReturnPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalLeaseReturnPlanStore(
    var items: MutableMap<String, RegionalLeaseReturnPlan>,
    var order: MutableList<String>,
) : RegionalLeaseReturnPlanStore {
    override fun find(code: String): RegionalLeaseReturnPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalLeaseReturnPlan) {
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

    override fun list(): MutableList<RegionalLeaseReturnPlan> {
        val out = mutableListOf<RegionalLeaseReturnPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalLeaseReturnPlanStore(): RegionalLeaseReturnPlanStore {
    return MemoryRegionalLeaseReturnPlanStore(
        mutableMapOf<String, RegionalLeaseReturnPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalLeaseReturnPlanStore(
    var inner: RegionalLeaseReturnPlanStore,
    var cache: TtlCache<RegionalLeaseReturnPlan>,
    var hits: Int,
    var misses: Int,
) : RegionalLeaseReturnPlanStore {
    override fun find(code: String): RegionalLeaseReturnPlan? {
        val hit = (cache.get(code) as RegionalLeaseReturnPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalLeaseReturnPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalLeaseReturnPlan> = inner.list()
}

fun newCachedRegionalLeaseReturnPlanStore(inner: RegionalLeaseReturnPlanStore, ttl: Int, clock: Clock): RegionalLeaseReturnPlanStore {
    return CachedRegionalLeaseReturnPlanStore(
        inner = inner,
        cache = newTtlCache<RegionalLeaseReturnPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional lease return plans. Empty fields match all. */
data class RegionalLeaseReturnPlanQuery(
    var category: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalLeaseReturnPlan): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalLeaseReturnPlan>): MutableList<RegionalLeaseReturnPlan> {
        val matched = filter(items) { candidate: RegionalLeaseReturnPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalLeaseReturnPlans(store: RegionalLeaseReturnPlanStore, query: RegionalLeaseReturnPlanQuery): MutableList<RegionalLeaseReturnPlan> {
    return query.apply(store.list())
}
