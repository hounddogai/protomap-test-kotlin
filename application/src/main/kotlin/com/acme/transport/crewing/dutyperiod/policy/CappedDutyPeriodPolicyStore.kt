package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped duty period policies by code. */
interface CappedDutyPeriodPolicyStore {
    fun find(code: String): CappedDutyPeriodPolicy?
    fun save(item: CappedDutyPeriodPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDutyPeriodPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDutyPeriodPolicyStore(
    var items: MutableMap<String, CappedDutyPeriodPolicy>,
    var order: MutableList<String>,
) : CappedDutyPeriodPolicyStore {
    override fun find(code: String): CappedDutyPeriodPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDutyPeriodPolicy) {
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

    override fun list(): MutableList<CappedDutyPeriodPolicy> {
        val out = mutableListOf<CappedDutyPeriodPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDutyPeriodPolicyStore(): CappedDutyPeriodPolicyStore {
    return MemoryCappedDutyPeriodPolicyStore(
        mutableMapOf<String, CappedDutyPeriodPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDutyPeriodPolicyStore(
    var inner: CappedDutyPeriodPolicyStore,
    var cache: TtlCache<CappedDutyPeriodPolicy>,
    var hits: Int,
    var misses: Int,
) : CappedDutyPeriodPolicyStore {
    override fun find(code: String): CappedDutyPeriodPolicy? {
        val hit = (cache.get(code) as CappedDutyPeriodPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDutyPeriodPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDutyPeriodPolicy> = inner.list()
}

fun newCachedCappedDutyPeriodPolicyStore(inner: CappedDutyPeriodPolicyStore, ttl: Int, clock: Clock): CappedDutyPeriodPolicyStore {
    return CachedCappedDutyPeriodPolicyStore(
        inner = inner,
        cache = newTtlCache<CappedDutyPeriodPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped duty period policies. Empty fields match all. */
data class CappedDutyPeriodPolicyQuery(
    var channel: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDutyPeriodPolicy): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<CappedDutyPeriodPolicy>): MutableList<CappedDutyPeriodPolicy> {
        val matched = filter(items) { candidate: CappedDutyPeriodPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDutyPeriodPolicies(store: CappedDutyPeriodPolicyStore, query: CappedDutyPeriodPolicyQuery): MutableList<CappedDutyPeriodPolicy> {
    return query.apply(store.list())
}
