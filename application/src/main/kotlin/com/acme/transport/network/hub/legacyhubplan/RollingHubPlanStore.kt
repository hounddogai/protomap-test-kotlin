package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling hub plans by code. */
interface RollingHubPlanStore {
    fun find(code: String): RollingHubPlan?
    fun save(item: RollingHubPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingHubPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingHubPlanStore(
    var items: MutableMap<String, RollingHubPlan>,
    var order: MutableList<String>,
) : RollingHubPlanStore {
    override fun find(code: String): RollingHubPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingHubPlan) {
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

    override fun list(): MutableList<RollingHubPlan> {
        val out = mutableListOf<RollingHubPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingHubPlanStore(): RollingHubPlanStore {
    return MemoryRollingHubPlanStore(mutableMapOf<String, RollingHubPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingHubPlanStore(
    var inner: RollingHubPlanStore,
    var cache: TtlCache<RollingHubPlan>,
    var hits: Int,
    var misses: Int,
) : RollingHubPlanStore {
    override fun find(code: String): RollingHubPlan? {
        val hit = (cache.get(code) as RollingHubPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingHubPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingHubPlan> = inner.list()
}

fun newCachedRollingHubPlanStore(inner: RollingHubPlanStore, ttl: Int, clock: Clock): RollingHubPlanStore {
    return CachedRollingHubPlanStore(
        inner = inner,
        cache = newTtlCache<RollingHubPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling hub plans. Empty fields match all. */
data class RollingHubPlanQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingHubPlan): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingHubPlan>): MutableList<RollingHubPlan> {
        val matched = filter(items) { candidate: RollingHubPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingHubPlans(store: RollingHubPlanStore, query: RollingHubPlanQuery): MutableList<RollingHubPlan> {
    return query.apply(store.list())
}
