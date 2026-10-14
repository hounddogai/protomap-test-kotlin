package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global route offset plans by code. */
interface GlobalRouteOffsetPlanStore {
    fun find(code: String): GlobalRouteOffsetPlan?
    fun save(item: GlobalRouteOffsetPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRouteOffsetPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRouteOffsetPlanStore(
    var items: MutableMap<String, GlobalRouteOffsetPlan>,
    var order: MutableList<String>,
) : GlobalRouteOffsetPlanStore {
    override fun find(code: String): GlobalRouteOffsetPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRouteOffsetPlan) {
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

    override fun list(): MutableList<GlobalRouteOffsetPlan> {
        val out = mutableListOf<GlobalRouteOffsetPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRouteOffsetPlanStore(): GlobalRouteOffsetPlanStore {
    return MemoryGlobalRouteOffsetPlanStore(
        mutableMapOf<String, GlobalRouteOffsetPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRouteOffsetPlanStore(
    var inner: GlobalRouteOffsetPlanStore,
    var cache: TtlCache<GlobalRouteOffsetPlan>,
    var hits: Int,
    var misses: Int,
) : GlobalRouteOffsetPlanStore {
    override fun find(code: String): GlobalRouteOffsetPlan? {
        val hit = (cache.get(code) as GlobalRouteOffsetPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRouteOffsetPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRouteOffsetPlan> = inner.list()
}

fun newCachedGlobalRouteOffsetPlanStore(inner: GlobalRouteOffsetPlanStore, ttl: Int, clock: Clock): GlobalRouteOffsetPlanStore {
    return CachedGlobalRouteOffsetPlanStore(
        inner = inner,
        cache = newTtlCache<GlobalRouteOffsetPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global route offset plans. Empty fields match all. */
data class GlobalRouteOffsetPlanQuery(
    var source: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRouteOffsetPlan): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRouteOffsetPlan>): MutableList<GlobalRouteOffsetPlan> {
        val matched = filter(items) { candidate: GlobalRouteOffsetPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRouteOffsetPlans(store: GlobalRouteOffsetPlanStore, query: GlobalRouteOffsetPlanQuery): MutableList<GlobalRouteOffsetPlan> {
    return query.apply(store.list())
}
