package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default utilization plans by code. */
interface DefaultUtilizationPlanStore {
    fun find(code: String): DefaultUtilizationPlan?
    fun save(item: DefaultUtilizationPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultUtilizationPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultUtilizationPlanStore(
    var items: MutableMap<String, DefaultUtilizationPlan>,
    var order: MutableList<String>,
) : DefaultUtilizationPlanStore {
    override fun find(code: String): DefaultUtilizationPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultUtilizationPlan) {
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

    override fun list(): MutableList<DefaultUtilizationPlan> {
        val out = mutableListOf<DefaultUtilizationPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultUtilizationPlanStore(): DefaultUtilizationPlanStore {
    return MemoryDefaultUtilizationPlanStore(
        mutableMapOf<String, DefaultUtilizationPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultUtilizationPlanStore(
    var inner: DefaultUtilizationPlanStore,
    var cache: TtlCache<DefaultUtilizationPlan>,
    var hits: Int,
    var misses: Int,
) : DefaultUtilizationPlanStore {
    override fun find(code: String): DefaultUtilizationPlan? {
        val hit = (cache.get(code) as DefaultUtilizationPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultUtilizationPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultUtilizationPlan> = inner.list()
}

fun newCachedDefaultUtilizationPlanStore(inner: DefaultUtilizationPlanStore, ttl: Int, clock: Clock): DefaultUtilizationPlanStore {
    return CachedDefaultUtilizationPlanStore(
        inner = inner,
        cache = newTtlCache<DefaultUtilizationPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default utilization plans. Empty fields match all. */
data class DefaultUtilizationPlanQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultUtilizationPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultUtilizationPlan>): MutableList<DefaultUtilizationPlan> {
        val matched = filter(items) { candidate: DefaultUtilizationPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultUtilizationPlans(store: DefaultUtilizationPlanStore, query: DefaultUtilizationPlanQuery): MutableList<DefaultUtilizationPlan> {
    return query.apply(store.list())
}
