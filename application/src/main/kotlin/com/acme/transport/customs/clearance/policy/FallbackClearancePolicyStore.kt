package com.acme.transport.customs.clearance.policy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback clearance policies by code. */
interface FallbackClearancePolicyStore {
    fun find(code: String): FallbackClearancePolicy?
    fun save(item: FallbackClearancePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackClearancePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackClearancePolicyStore(
    var items: MutableMap<String, FallbackClearancePolicy>,
    var order: MutableList<String>,
) : FallbackClearancePolicyStore {
    override fun find(code: String): FallbackClearancePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackClearancePolicy) {
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

    override fun list(): MutableList<FallbackClearancePolicy> {
        val out = mutableListOf<FallbackClearancePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackClearancePolicyStore(): FallbackClearancePolicyStore {
    return MemoryFallbackClearancePolicyStore(
        mutableMapOf<String, FallbackClearancePolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackClearancePolicyStore(
    var inner: FallbackClearancePolicyStore,
    var cache: TtlCache<FallbackClearancePolicy>,
    var hits: Int,
    var misses: Int,
) : FallbackClearancePolicyStore {
    override fun find(code: String): FallbackClearancePolicy? {
        val hit = (cache.get(code) as FallbackClearancePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackClearancePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackClearancePolicy> = inner.list()
}

fun newCachedFallbackClearancePolicyStore(inner: FallbackClearancePolicyStore, ttl: Int, clock: Clock): FallbackClearancePolicyStore {
    return CachedFallbackClearancePolicyStore(
        inner = inner,
        cache = newTtlCache<FallbackClearancePolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback clearance policies. Empty fields match all. */
data class FallbackClearancePolicyQuery(
    var locale: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackClearancePolicy): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<FallbackClearancePolicy>): MutableList<FallbackClearancePolicy> {
        val matched = filter(items) { candidate: FallbackClearancePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackClearancePolicies(store: FallbackClearancePolicyStore, query: FallbackClearancePolicyQuery): MutableList<FallbackClearancePolicy> {
    return query.apply(store.list())
}
