package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive capacity policies by code. */
interface AdaptiveCapacityPolicyStore {
    fun find(code: String): AdaptiveCapacityPolicy?
    fun save(item: AdaptiveCapacityPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCapacityPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCapacityPolicyStore(
    var items: MutableMap<String, AdaptiveCapacityPolicy>,
    var order: MutableList<String>,
) : AdaptiveCapacityPolicyStore {
    override fun find(code: String): AdaptiveCapacityPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCapacityPolicy) {
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

    override fun list(): MutableList<AdaptiveCapacityPolicy> {
        val out = mutableListOf<AdaptiveCapacityPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCapacityPolicyStore(): AdaptiveCapacityPolicyStore {
    return MemoryAdaptiveCapacityPolicyStore(
        mutableMapOf<String, AdaptiveCapacityPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCapacityPolicyStore(
    var inner: AdaptiveCapacityPolicyStore,
    var cache: TtlCache<AdaptiveCapacityPolicy>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCapacityPolicyStore {
    override fun find(code: String): AdaptiveCapacityPolicy? {
        val hit = (cache.get(code) as AdaptiveCapacityPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCapacityPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCapacityPolicy> = inner.list()
}

fun newCachedAdaptiveCapacityPolicyStore(inner: AdaptiveCapacityPolicyStore, ttl: Int, clock: Clock): AdaptiveCapacityPolicyStore {
    return CachedAdaptiveCapacityPolicyStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCapacityPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive capacity policies. Empty fields match all. */
data class AdaptiveCapacityPolicyQuery(
    var segment: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCapacityPolicy): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveCapacityPolicy>): MutableList<AdaptiveCapacityPolicy> {
        val matched = filter(items) { candidate: AdaptiveCapacityPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCapacityPolicies(store: AdaptiveCapacityPolicyStore, query: AdaptiveCapacityPolicyQuery): MutableList<AdaptiveCapacityPolicy> {
    return query.apply(store.list())
}
