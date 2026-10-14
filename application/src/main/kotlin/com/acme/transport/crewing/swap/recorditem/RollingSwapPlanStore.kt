package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling swap plans by code. */
interface RollingSwapPlanStore {
    fun find(code: String): RollingSwapPlan?
    fun save(item: RollingSwapPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingSwapPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingSwapPlanStore(
    var items: MutableMap<String, RollingSwapPlan>,
    var order: MutableList<String>,
) : RollingSwapPlanStore {
    override fun find(code: String): RollingSwapPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingSwapPlan) {
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

    override fun list(): MutableList<RollingSwapPlan> {
        val out = mutableListOf<RollingSwapPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingSwapPlanStore(): RollingSwapPlanStore {
    return MemoryRollingSwapPlanStore(mutableMapOf<String, RollingSwapPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingSwapPlanStore(
    var inner: RollingSwapPlanStore,
    var cache: TtlCache<RollingSwapPlan>,
    var hits: Int,
    var misses: Int,
) : RollingSwapPlanStore {
    override fun find(code: String): RollingSwapPlan? {
        val hit = (cache.get(code) as RollingSwapPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingSwapPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingSwapPlan> = inner.list()
}

fun newCachedRollingSwapPlanStore(inner: RollingSwapPlanStore, ttl: Int, clock: Clock): RollingSwapPlanStore {
    return CachedRollingSwapPlanStore(
        inner = inner,
        cache = newTtlCache<RollingSwapPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling swap plans. Empty fields match all. */
data class RollingSwapPlanQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingSwapPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RollingSwapPlan>): MutableList<RollingSwapPlan> {
        val matched = filter(items) { candidate: RollingSwapPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingSwapPlans(store: RollingSwapPlanStore, query: RollingSwapPlanQuery): MutableList<RollingSwapPlan> {
    return query.apply(store.list())
}
