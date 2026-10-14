package com.acme.transport.fleetops.utilization.utilizationvariance

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient utilization plans by code. */
interface LenientUtilizationPlanStore {
    fun find(code: String): LenientUtilizationPlan?
    fun save(item: LenientUtilizationPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientUtilizationPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientUtilizationPlanStore(
    var items: MutableMap<String, LenientUtilizationPlan>,
    var order: MutableList<String>,
) : LenientUtilizationPlanStore {
    override fun find(code: String): LenientUtilizationPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientUtilizationPlan) {
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

    override fun list(): MutableList<LenientUtilizationPlan> {
        val out = mutableListOf<LenientUtilizationPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientUtilizationPlanStore(): LenientUtilizationPlanStore {
    return MemoryLenientUtilizationPlanStore(
        mutableMapOf<String, LenientUtilizationPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientUtilizationPlanStore(
    var inner: LenientUtilizationPlanStore,
    var cache: TtlCache<LenientUtilizationPlan>,
    var hits: Int,
    var misses: Int,
) : LenientUtilizationPlanStore {
    override fun find(code: String): LenientUtilizationPlan? {
        val hit = (cache.get(code) as LenientUtilizationPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientUtilizationPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientUtilizationPlan> = inner.list()
}

fun newCachedLenientUtilizationPlanStore(inner: LenientUtilizationPlanStore, ttl: Int, clock: Clock): LenientUtilizationPlanStore {
    return CachedLenientUtilizationPlanStore(
        inner = inner,
        cache = newTtlCache<LenientUtilizationPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient utilization plans. Empty fields match all. */
data class LenientUtilizationPlanQuery(
    var depot: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientUtilizationPlan): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<LenientUtilizationPlan>): MutableList<LenientUtilizationPlan> {
        val matched = filter(items) { candidate: LenientUtilizationPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientUtilizationPlans(store: LenientUtilizationPlanStore, query: LenientUtilizationPlanQuery): MutableList<LenientUtilizationPlan> {
    return query.apply(store.list())
}
