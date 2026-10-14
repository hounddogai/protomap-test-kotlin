package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity plans by code. */
interface CapacityPlanStore {
    fun find(code: String): CapacityPlan?
    fun save(item: CapacityPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityPlanStore(
    var items: MutableMap<String, CapacityPlan>,
    var order: MutableList<String>,
) : CapacityPlanStore {
    override fun find(code: String): CapacityPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityPlan) {
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

    override fun list(): MutableList<CapacityPlan> {
        val out = mutableListOf<CapacityPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityPlanStore(): CapacityPlanStore {
    return MemoryCapacityPlanStore(mutableMapOf<String, CapacityPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityPlanStore(
    var inner: CapacityPlanStore,
    var cache: TtlCache<CapacityPlan>,
    var hits: Int,
    var misses: Int,
) : CapacityPlanStore {
    override fun find(code: String): CapacityPlan? {
        val hit = (cache.get(code) as CapacityPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityPlan> = inner.list()
}

fun newCachedCapacityPlanStore(inner: CapacityPlanStore, ttl: Int, clock: Clock): CapacityPlanStore {
    return CachedCapacityPlanStore(
        inner = inner,
        cache = newTtlCache<CapacityPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity plans. Empty fields match all. */
data class CapacityPlanQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityPlan): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityPlan>): MutableList<CapacityPlan> {
        val matched = filter(items) { candidate: CapacityPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityPlans(store: CapacityPlanStore, query: CapacityPlanQuery): MutableList<CapacityPlan> {
    return query.apply(store.list())
}
