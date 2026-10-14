package com.acme.transport.network.hub.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub plans by code. */
interface HubPlanStore {
    fun find(code: String): HubPlan?
    fun save(item: HubPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubPlanStore(
    var items: MutableMap<String, HubPlan>,
    var order: MutableList<String>,
) : HubPlanStore {
    override fun find(code: String): HubPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubPlan) {
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

    override fun list(): MutableList<HubPlan> {
        val out = mutableListOf<HubPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubPlanStore(): HubPlanStore {
    return MemoryHubPlanStore(mutableMapOf<String, HubPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubPlanStore(
    var inner: HubPlanStore,
    var cache: TtlCache<HubPlan>,
    var hits: Int,
    var misses: Int,
) : HubPlanStore {
    override fun find(code: String): HubPlan? {
        val hit = (cache.get(code) as HubPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubPlan> = inner.list()
}

fun newCachedHubPlanStore(inner: HubPlanStore, ttl: Int, clock: Clock): HubPlanStore {
    return CachedHubPlanStore(
        inner = inner,
        cache = newTtlCache<HubPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub plans. Empty fields match all. */
data class HubPlanQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubPlan): Boolean {
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

    fun apply(items: MutableList<HubPlan>): MutableList<HubPlan> {
        val matched = filter(items) { candidate: HubPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubPlans(store: HubPlanStore, query: HubPlanQuery): MutableList<HubPlan> = query.apply(store.list())
