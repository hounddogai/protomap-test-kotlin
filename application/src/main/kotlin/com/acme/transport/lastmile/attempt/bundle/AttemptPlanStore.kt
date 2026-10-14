package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt plans by code. */
interface AttemptPlanStore {
    fun find(code: String): AttemptPlan?
    fun save(item: AttemptPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptPlanStore(
    var items: MutableMap<String, AttemptPlan>,
    var order: MutableList<String>,
) : AttemptPlanStore {
    override fun find(code: String): AttemptPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptPlan) {
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

    override fun list(): MutableList<AttemptPlan> {
        val out = mutableListOf<AttemptPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptPlanStore(): AttemptPlanStore {
    return MemoryAttemptPlanStore(mutableMapOf<String, AttemptPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptPlanStore(
    var inner: AttemptPlanStore,
    var cache: TtlCache<AttemptPlan>,
    var hits: Int,
    var misses: Int,
) : AttemptPlanStore {
    override fun find(code: String): AttemptPlan? {
        val hit = (cache.get(code) as AttemptPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptPlan> = inner.list()
}

fun newCachedAttemptPlanStore(inner: AttemptPlanStore, ttl: Int, clock: Clock): AttemptPlanStore {
    return CachedAttemptPlanStore(
        inner = inner,
        cache = newTtlCache<AttemptPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt plans. Empty fields match all. */
data class AttemptPlanQuery(
    var segment: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptPlan): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptPlan>): MutableList<AttemptPlan> {
        val matched = filter(items) { candidate: AttemptPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptPlans(store: AttemptPlanStore, query: AttemptPlanQuery): MutableList<AttemptPlan> {
    return query.apply(store.list())
}
