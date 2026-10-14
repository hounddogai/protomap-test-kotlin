package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict attempt plans by code. */
interface StrictAttemptPlanStore {
    fun find(code: String): StrictAttemptPlan?
    fun save(item: StrictAttemptPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAttemptPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAttemptPlanStore(
    var items: MutableMap<String, StrictAttemptPlan>,
    var order: MutableList<String>,
) : StrictAttemptPlanStore {
    override fun find(code: String): StrictAttemptPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAttemptPlan) {
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

    override fun list(): MutableList<StrictAttemptPlan> {
        val out = mutableListOf<StrictAttemptPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAttemptPlanStore(): StrictAttemptPlanStore {
    return MemoryStrictAttemptPlanStore(mutableMapOf<String, StrictAttemptPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAttemptPlanStore(
    var inner: StrictAttemptPlanStore,
    var cache: TtlCache<StrictAttemptPlan>,
    var hits: Int,
    var misses: Int,
) : StrictAttemptPlanStore {
    override fun find(code: String): StrictAttemptPlan? {
        val hit = (cache.get(code) as StrictAttemptPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAttemptPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAttemptPlan> = inner.list()
}

fun newCachedStrictAttemptPlanStore(inner: StrictAttemptPlanStore, ttl: Int, clock: Clock): StrictAttemptPlanStore {
    return CachedStrictAttemptPlanStore(
        inner = inner,
        cache = newTtlCache<StrictAttemptPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict attempt plans. Empty fields match all. */
data class StrictAttemptPlanQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAttemptPlan): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<StrictAttemptPlan>): MutableList<StrictAttemptPlan> {
        val matched = filter(items) { candidate: StrictAttemptPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAttemptPlans(store: StrictAttemptPlanStore, query: StrictAttemptPlanQuery): MutableList<StrictAttemptPlan> {
    return query.apply(store.list())
}
