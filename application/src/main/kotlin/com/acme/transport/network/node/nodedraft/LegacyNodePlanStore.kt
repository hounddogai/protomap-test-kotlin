package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps legacy node plans by code. */
interface LegacyNodePlanStore {
    fun find(code: String): LegacyNodePlan?
    fun save(item: LegacyNodePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyNodePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyNodePlanStore(
    var items: MutableMap<String, LegacyNodePlan>,
    var order: MutableList<String>,
) : LegacyNodePlanStore {
    override fun find(code: String): LegacyNodePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyNodePlan) {
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

    override fun list(): MutableList<LegacyNodePlan> {
        val out = mutableListOf<LegacyNodePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyNodePlanStore(): LegacyNodePlanStore {
    return MemoryLegacyNodePlanStore(mutableMapOf<String, LegacyNodePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyNodePlanStore(
    var inner: LegacyNodePlanStore,
    var cache: TtlCache<LegacyNodePlan>,
    var hits: Int,
    var misses: Int,
) : LegacyNodePlanStore {
    override fun find(code: String): LegacyNodePlan? {
        val hit = (cache.get(code) as LegacyNodePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyNodePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyNodePlan> = inner.list()
}

fun newCachedLegacyNodePlanStore(inner: LegacyNodePlanStore, ttl: Int, clock: Clock): LegacyNodePlanStore {
    return CachedLegacyNodePlanStore(
        inner = inner,
        cache = newTtlCache<LegacyNodePlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy node plans. Empty fields match all. */
data class LegacyNodePlanQuery(
    var country: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyNodePlan): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyNodePlan>): MutableList<LegacyNodePlan> {
        val matched = filter(items) { candidate: LegacyNodePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyNodePlans(store: LegacyNodePlanStore, query: LegacyNodePlanQuery): MutableList<LegacyNodePlan> {
    return query.apply(store.list())
}
