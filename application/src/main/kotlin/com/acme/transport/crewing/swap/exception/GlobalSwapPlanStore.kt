package com.acme.transport.crewing.swap.exception

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global swap plans by code. */
interface GlobalSwapPlanStore {
    fun find(code: String): GlobalSwapPlan?
    fun save(item: GlobalSwapPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalSwapPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalSwapPlanStore(
    var items: MutableMap<String, GlobalSwapPlan>,
    var order: MutableList<String>,
) : GlobalSwapPlanStore {
    override fun find(code: String): GlobalSwapPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalSwapPlan) {
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

    override fun list(): MutableList<GlobalSwapPlan> {
        val out = mutableListOf<GlobalSwapPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalSwapPlanStore(): GlobalSwapPlanStore {
    return MemoryGlobalSwapPlanStore(mutableMapOf<String, GlobalSwapPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalSwapPlanStore(
    var inner: GlobalSwapPlanStore,
    var cache: TtlCache<GlobalSwapPlan>,
    var hits: Int,
    var misses: Int,
) : GlobalSwapPlanStore {
    override fun find(code: String): GlobalSwapPlan? {
        val hit = (cache.get(code) as GlobalSwapPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalSwapPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalSwapPlan> = inner.list()
}

fun newCachedGlobalSwapPlanStore(inner: GlobalSwapPlanStore, ttl: Int, clock: Clock): GlobalSwapPlanStore {
    return CachedGlobalSwapPlanStore(
        inner = inner,
        cache = newTtlCache<GlobalSwapPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global swap plans. Empty fields match all. */
data class GlobalSwapPlanQuery(
    var segment: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalSwapPlan): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalSwapPlan>): MutableList<GlobalSwapPlan> {
        val matched = filter(items) { candidate: GlobalSwapPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalSwapPlans(store: GlobalSwapPlanStore, query: GlobalSwapPlanQuery): MutableList<GlobalSwapPlan> {
    return query.apply(store.list())
}
