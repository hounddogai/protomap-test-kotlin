package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled scorecard policies by code. */
interface ScheduledScorecardPolicyStore {
    fun find(code: String): ScheduledScorecardPolicy?
    fun save(item: ScheduledScorecardPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledScorecardPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledScorecardPolicyStore(
    var items: MutableMap<String, ScheduledScorecardPolicy>,
    var order: MutableList<String>,
) : ScheduledScorecardPolicyStore {
    override fun find(code: String): ScheduledScorecardPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledScorecardPolicy) {
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

    override fun list(): MutableList<ScheduledScorecardPolicy> {
        val out = mutableListOf<ScheduledScorecardPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledScorecardPolicyStore(): ScheduledScorecardPolicyStore {
    return MemoryScheduledScorecardPolicyStore(
        mutableMapOf<String, ScheduledScorecardPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledScorecardPolicyStore(
    var inner: ScheduledScorecardPolicyStore,
    var cache: TtlCache<ScheduledScorecardPolicy>,
    var hits: Int,
    var misses: Int,
) : ScheduledScorecardPolicyStore {
    override fun find(code: String): ScheduledScorecardPolicy? {
        val hit = (cache.get(code) as ScheduledScorecardPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledScorecardPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledScorecardPolicy> = inner.list()
}

fun newCachedScheduledScorecardPolicyStore(inner: ScheduledScorecardPolicyStore, ttl: Int, clock: Clock): ScheduledScorecardPolicyStore {
    return CachedScheduledScorecardPolicyStore(
        inner = inner,
        cache = newTtlCache<ScheduledScorecardPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled scorecard policies. Empty fields match all. */
data class ScheduledScorecardPolicyQuery(
    var depot: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledScorecardPolicy): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<ScheduledScorecardPolicy>): MutableList<ScheduledScorecardPolicy> {
        val matched = filter(items) { candidate: ScheduledScorecardPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledScorecardPolicies(store: ScheduledScorecardPolicyStore, query: ScheduledScorecardPolicyQuery): MutableList<ScheduledScorecardPolicy> {
    return query.apply(store.list())
}
