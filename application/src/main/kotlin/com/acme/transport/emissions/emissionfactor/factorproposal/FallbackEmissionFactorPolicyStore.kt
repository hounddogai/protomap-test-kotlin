package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback emission factor policies by code. */
interface FallbackEmissionFactorPolicyStore {
    fun find(code: String): FallbackEmissionFactorPolicy?
    fun save(item: FallbackEmissionFactorPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackEmissionFactorPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackEmissionFactorPolicyStore(
    var items: MutableMap<String, FallbackEmissionFactorPolicy>,
    var order: MutableList<String>,
) : FallbackEmissionFactorPolicyStore {
    override fun find(code: String): FallbackEmissionFactorPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackEmissionFactorPolicy) {
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

    override fun list(): MutableList<FallbackEmissionFactorPolicy> {
        val out = mutableListOf<FallbackEmissionFactorPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackEmissionFactorPolicyStore(): FallbackEmissionFactorPolicyStore {
    return MemoryFallbackEmissionFactorPolicyStore(
        mutableMapOf<String, FallbackEmissionFactorPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackEmissionFactorPolicyStore(
    var inner: FallbackEmissionFactorPolicyStore,
    var cache: TtlCache<FallbackEmissionFactorPolicy>,
    var hits: Int,
    var misses: Int,
) : FallbackEmissionFactorPolicyStore {
    override fun find(code: String): FallbackEmissionFactorPolicy? {
        val hit = (cache.get(code) as FallbackEmissionFactorPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackEmissionFactorPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackEmissionFactorPolicy> = inner.list()
}

fun newCachedFallbackEmissionFactorPolicyStore(inner: FallbackEmissionFactorPolicyStore, ttl: Int, clock: Clock): FallbackEmissionFactorPolicyStore {
    return CachedFallbackEmissionFactorPolicyStore(
        inner = inner,
        cache = newTtlCache<FallbackEmissionFactorPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback emission factor policies. Empty fields match all. */
data class FallbackEmissionFactorPolicyQuery(
    var region: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackEmissionFactorPolicy): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackEmissionFactorPolicy>): MutableList<FallbackEmissionFactorPolicy> {
        val matched = filter(items) { candidate: FallbackEmissionFactorPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackEmissionFactorPolicies(store: FallbackEmissionFactorPolicyStore, query: FallbackEmissionFactorPolicyQuery): MutableList<FallbackEmissionFactorPolicy> {
    return query.apply(store.list())
}
