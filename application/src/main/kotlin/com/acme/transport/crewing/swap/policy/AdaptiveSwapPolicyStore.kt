package com.acme.transport.crewing.swap.policy

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive swap policies by code. */
interface AdaptiveSwapPolicyStore {
    fun find(code: String): AdaptiveSwapPolicy?
    fun save(item: AdaptiveSwapPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveSwapPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveSwapPolicyStore(
    var items: MutableMap<String, AdaptiveSwapPolicy>,
    var order: MutableList<String>,
) : AdaptiveSwapPolicyStore {
    override fun find(code: String): AdaptiveSwapPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveSwapPolicy) {
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

    override fun list(): MutableList<AdaptiveSwapPolicy> {
        val out = mutableListOf<AdaptiveSwapPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveSwapPolicyStore(): AdaptiveSwapPolicyStore {
    return MemoryAdaptiveSwapPolicyStore(mutableMapOf<String, AdaptiveSwapPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveSwapPolicyStore(
    var inner: AdaptiveSwapPolicyStore,
    var cache: TtlCache<AdaptiveSwapPolicy>,
    var hits: Int,
    var misses: Int,
) : AdaptiveSwapPolicyStore {
    override fun find(code: String): AdaptiveSwapPolicy? {
        val hit = (cache.get(code) as AdaptiveSwapPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveSwapPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveSwapPolicy> = inner.list()
}

fun newCachedAdaptiveSwapPolicyStore(inner: AdaptiveSwapPolicyStore, ttl: Int, clock: Clock): AdaptiveSwapPolicyStore {
    return CachedAdaptiveSwapPolicyStore(
        inner = inner,
        cache = newTtlCache<AdaptiveSwapPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive swap policies. Empty fields match all. */
data class AdaptiveSwapPolicyQuery(
    var country: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveSwapPolicy): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveSwapPolicy>): MutableList<AdaptiveSwapPolicy> {
        val matched = filter(items) { candidate: AdaptiveSwapPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveSwapPolicies(store: AdaptiveSwapPolicyStore, query: AdaptiveSwapPolicyQuery): MutableList<AdaptiveSwapPolicy> {
    return query.apply(store.list())
}
