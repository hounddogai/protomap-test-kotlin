package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional node plans by code. */
interface RegionalNodePlanStore {
    fun find(code: String): RegionalNodePlan?
    fun save(item: RegionalNodePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalNodePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalNodePlanStore(
    var items: MutableMap<String, RegionalNodePlan>,
    var order: MutableList<String>,
) : RegionalNodePlanStore {
    override fun find(code: String): RegionalNodePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalNodePlan) {
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

    override fun list(): MutableList<RegionalNodePlan> {
        val out = mutableListOf<RegionalNodePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalNodePlanStore(): RegionalNodePlanStore {
    return MemoryRegionalNodePlanStore(mutableMapOf<String, RegionalNodePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalNodePlanStore(
    var inner: RegionalNodePlanStore,
    var cache: TtlCache<RegionalNodePlan>,
    var hits: Int,
    var misses: Int,
) : RegionalNodePlanStore {
    override fun find(code: String): RegionalNodePlan? {
        val hit = (cache.get(code) as RegionalNodePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalNodePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalNodePlan> = inner.list()
}

fun newCachedRegionalNodePlanStore(inner: RegionalNodePlanStore, ttl: Int, clock: Clock): RegionalNodePlanStore {
    return CachedRegionalNodePlanStore(
        inner = inner,
        cache = newTtlCache<RegionalNodePlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional node plans. Empty fields match all. */
data class RegionalNodePlanQuery(
    var lane: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalNodePlan): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalNodePlan>): MutableList<RegionalNodePlan> {
        val matched = filter(items) { candidate: RegionalNodePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalNodePlans(store: RegionalNodePlanStore, query: RegionalNodePlanQuery): MutableList<RegionalNodePlan> {
    return query.apply(store.list())
}
