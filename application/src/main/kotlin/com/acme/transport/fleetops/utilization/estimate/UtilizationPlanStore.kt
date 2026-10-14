package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization plans by code. */
interface UtilizationPlanStore {
    fun find(code: String): UtilizationPlan?
    fun save(item: UtilizationPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationPlanStore(
    var items: MutableMap<String, UtilizationPlan>,
    var order: MutableList<String>,
) : UtilizationPlanStore {
    override fun find(code: String): UtilizationPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationPlan) {
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

    override fun list(): MutableList<UtilizationPlan> {
        val out = mutableListOf<UtilizationPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationPlanStore(): UtilizationPlanStore {
    return MemoryUtilizationPlanStore(mutableMapOf<String, UtilizationPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationPlanStore(
    var inner: UtilizationPlanStore,
    var cache: TtlCache<UtilizationPlan>,
    var hits: Int,
    var misses: Int,
) : UtilizationPlanStore {
    override fun find(code: String): UtilizationPlan? {
        val hit = (cache.get(code) as UtilizationPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationPlan> = inner.list()
}

fun newCachedUtilizationPlanStore(inner: UtilizationPlanStore, ttl: Int, clock: Clock): UtilizationPlanStore {
    return CachedUtilizationPlanStore(
        inner = inner,
        cache = newTtlCache<UtilizationPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization plans. Empty fields match all. */
data class UtilizationPlanQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationPlan): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationPlan>): MutableList<UtilizationPlan> {
        val matched = filter(items) { candidate: UtilizationPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationPlans(store: UtilizationPlanStore, query: UtilizationPlanQuery): MutableList<UtilizationPlan> {
    return query.apply(store.list())
}
