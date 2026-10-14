package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break plans by code. */
interface RestBreakPlanStore {
    fun find(code: String): RestBreakPlan?
    fun save(item: RestBreakPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakPlanStore(
    var items: MutableMap<String, RestBreakPlan>,
    var order: MutableList<String>,
) : RestBreakPlanStore {
    override fun find(code: String): RestBreakPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakPlan) {
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

    override fun list(): MutableList<RestBreakPlan> {
        val out = mutableListOf<RestBreakPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakPlanStore(): RestBreakPlanStore {
    return MemoryRestBreakPlanStore(mutableMapOf<String, RestBreakPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakPlanStore(
    var inner: RestBreakPlanStore,
    var cache: TtlCache<RestBreakPlan>,
    var hits: Int,
    var misses: Int,
) : RestBreakPlanStore {
    override fun find(code: String): RestBreakPlan? {
        val hit = (cache.get(code) as RestBreakPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakPlan> = inner.list()
}

fun newCachedRestBreakPlanStore(inner: RestBreakPlanStore, ttl: Int, clock: Clock): RestBreakPlanStore {
    return CachedRestBreakPlanStore(
        inner = inner,
        cache = newTtlCache<RestBreakPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break plans. Empty fields match all. */
data class RestBreakPlanQuery(
    var source: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakPlan): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakPlan>): MutableList<RestBreakPlan> {
        val matched = filter(items) { candidate: RestBreakPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakPlans(store: RestBreakPlanStore, query: RestBreakPlanQuery): MutableList<RestBreakPlan> {
    return query.apply(store.list())
}
