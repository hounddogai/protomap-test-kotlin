package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff plans by code. */
interface CutoffPlanStore {
    fun find(code: String): CutoffPlan?
    fun save(item: CutoffPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffPlanStore(
    var items: MutableMap<String, CutoffPlan>,
    var order: MutableList<String>,
) : CutoffPlanStore {
    override fun find(code: String): CutoffPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffPlan) {
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

    override fun list(): MutableList<CutoffPlan> {
        val out = mutableListOf<CutoffPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffPlanStore(): CutoffPlanStore {
    return MemoryCutoffPlanStore(mutableMapOf<String, CutoffPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffPlanStore(
    var inner: CutoffPlanStore,
    var cache: TtlCache<CutoffPlan>,
    var hits: Int,
    var misses: Int,
) : CutoffPlanStore {
    override fun find(code: String): CutoffPlan? {
        val hit = (cache.get(code) as CutoffPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffPlan> = inner.list()
}

fun newCachedCutoffPlanStore(inner: CutoffPlanStore, ttl: Int, clock: Clock): CutoffPlanStore {
    return CachedCutoffPlanStore(
        inner = inner,
        cache = newTtlCache<CutoffPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff plans. Empty fields match all. */
data class CutoffPlanQuery(
    var source: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffPlan): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<CutoffPlan>): MutableList<CutoffPlan> {
        val matched = filter(items) { candidate: CutoffPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffPlans(store: CutoffPlanStore, query: CutoffPlanQuery): MutableList<CutoffPlan> {
    return query.apply(store.list())
}
