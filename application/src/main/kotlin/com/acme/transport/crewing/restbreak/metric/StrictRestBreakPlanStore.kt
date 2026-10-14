package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict rest break plans by code. */
interface StrictRestBreakPlanStore {
    fun find(code: String): StrictRestBreakPlan?
    fun save(item: StrictRestBreakPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRestBreakPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRestBreakPlanStore(
    var items: MutableMap<String, StrictRestBreakPlan>,
    var order: MutableList<String>,
) : StrictRestBreakPlanStore {
    override fun find(code: String): StrictRestBreakPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRestBreakPlan) {
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

    override fun list(): MutableList<StrictRestBreakPlan> {
        val out = mutableListOf<StrictRestBreakPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRestBreakPlanStore(): StrictRestBreakPlanStore {
    return MemoryStrictRestBreakPlanStore(mutableMapOf<String, StrictRestBreakPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRestBreakPlanStore(
    var inner: StrictRestBreakPlanStore,
    var cache: TtlCache<StrictRestBreakPlan>,
    var hits: Int,
    var misses: Int,
) : StrictRestBreakPlanStore {
    override fun find(code: String): StrictRestBreakPlan? {
        val hit = (cache.get(code) as StrictRestBreakPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRestBreakPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRestBreakPlan> = inner.list()
}

fun newCachedStrictRestBreakPlanStore(inner: StrictRestBreakPlanStore, ttl: Int, clock: Clock): StrictRestBreakPlanStore {
    return CachedStrictRestBreakPlanStore(
        inner = inner,
        cache = newTtlCache<StrictRestBreakPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict rest break plans. Empty fields match all. */
data class StrictRestBreakPlanQuery(
    var category: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRestBreakPlan): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRestBreakPlan>): MutableList<StrictRestBreakPlan> {
        val matched = filter(items) { candidate: StrictRestBreakPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRestBreakPlans(store: StrictRestBreakPlanStore, query: StrictRestBreakPlanQuery): MutableList<StrictRestBreakPlan> {
    return query.apply(store.list())
}
