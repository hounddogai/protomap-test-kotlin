package com.acme.transport.safety.coaching.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching plans by code. */
interface CoachingPlanStore {
    fun find(code: String): CoachingPlan?
    fun save(item: CoachingPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingPlanStore(
    var items: MutableMap<String, CoachingPlan>,
    var order: MutableList<String>,
) : CoachingPlanStore {
    override fun find(code: String): CoachingPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingPlan) {
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

    override fun list(): MutableList<CoachingPlan> {
        val out = mutableListOf<CoachingPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingPlanStore(): CoachingPlanStore {
    return MemoryCoachingPlanStore(mutableMapOf<String, CoachingPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingPlanStore(
    var inner: CoachingPlanStore,
    var cache: TtlCache<CoachingPlan>,
    var hits: Int,
    var misses: Int,
) : CoachingPlanStore {
    override fun find(code: String): CoachingPlan? {
        val hit = (cache.get(code) as CoachingPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingPlan> = inner.list()
}

fun newCachedCoachingPlanStore(inner: CoachingPlanStore, ttl: Int, clock: Clock): CoachingPlanStore {
    return CachedCoachingPlanStore(
        inner = inner,
        cache = newTtlCache<CoachingPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching plans. Empty fields match all. */
data class CoachingPlanQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingPlan>): MutableList<CoachingPlan> {
        val matched = filter(items) { candidate: CoachingPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingPlans(store: CoachingPlanStore, query: CoachingPlanQuery): MutableList<CoachingPlan> {
    return query.apply(store.list())
}
