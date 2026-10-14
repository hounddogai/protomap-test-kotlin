package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard plans by code. */
interface ScorecardPlanStore {
    fun find(code: String): ScorecardPlan?
    fun save(item: ScorecardPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardPlanStore(
    var items: MutableMap<String, ScorecardPlan>,
    var order: MutableList<String>,
) : ScorecardPlanStore {
    override fun find(code: String): ScorecardPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardPlan) {
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

    override fun list(): MutableList<ScorecardPlan> {
        val out = mutableListOf<ScorecardPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardPlanStore(): ScorecardPlanStore {
    return MemoryScorecardPlanStore(mutableMapOf<String, ScorecardPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardPlanStore(
    var inner: ScorecardPlanStore,
    var cache: TtlCache<ScorecardPlan>,
    var hits: Int,
    var misses: Int,
) : ScorecardPlanStore {
    override fun find(code: String): ScorecardPlan? {
        val hit = (cache.get(code) as ScorecardPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardPlan> = inner.list()
}

fun newCachedScorecardPlanStore(inner: ScorecardPlanStore, ttl: Int, clock: Clock): ScorecardPlanStore {
    return CachedScorecardPlanStore(
        inner = inner,
        cache = newTtlCache<ScorecardPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard plans. Empty fields match all. */
data class ScorecardPlanQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardPlan): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardPlan>): MutableList<ScorecardPlan> {
        val matched = filter(items) { candidate: ScorecardPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardPlans(store: ScorecardPlanStore, query: ScorecardPlanQuery): MutableList<ScorecardPlan> {
    return query.apply(store.list())
}
