package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted audit policies by code. */
interface WeightedAuditPolicyStore {
    fun find(code: String): WeightedAuditPolicy?
    fun save(item: WeightedAuditPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAuditPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAuditPolicyStore(
    var items: MutableMap<String, WeightedAuditPolicy>,
    var order: MutableList<String>,
) : WeightedAuditPolicyStore {
    override fun find(code: String): WeightedAuditPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAuditPolicy) {
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

    override fun list(): MutableList<WeightedAuditPolicy> {
        val out = mutableListOf<WeightedAuditPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAuditPolicyStore(): WeightedAuditPolicyStore {
    return MemoryWeightedAuditPolicyStore(mutableMapOf<String, WeightedAuditPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAuditPolicyStore(
    var inner: WeightedAuditPolicyStore,
    var cache: TtlCache<WeightedAuditPolicy>,
    var hits: Int,
    var misses: Int,
) : WeightedAuditPolicyStore {
    override fun find(code: String): WeightedAuditPolicy? {
        val hit = (cache.get(code) as WeightedAuditPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAuditPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAuditPolicy> = inner.list()
}

fun newCachedWeightedAuditPolicyStore(inner: WeightedAuditPolicyStore, ttl: Int, clock: Clock): WeightedAuditPolicyStore {
    return CachedWeightedAuditPolicyStore(
        inner = inner,
        cache = newTtlCache<WeightedAuditPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted audit policies. Empty fields match all. */
data class WeightedAuditPolicyQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAuditPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAuditPolicy>): MutableList<WeightedAuditPolicy> {
        val matched = filter(items) { candidate: WeightedAuditPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAuditPolicies(store: WeightedAuditPolicyStore, query: WeightedAuditPolicyQuery): MutableList<WeightedAuditPolicy> {
    return query.apply(store.list())
}
