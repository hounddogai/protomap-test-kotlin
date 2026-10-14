package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional violation policies by code. */
interface RegionalViolationPolicyStore {
    fun find(code: String): RegionalViolationPolicy?
    fun save(item: RegionalViolationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalViolationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalViolationPolicyStore(
    var items: MutableMap<String, RegionalViolationPolicy>,
    var order: MutableList<String>,
) : RegionalViolationPolicyStore {
    override fun find(code: String): RegionalViolationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalViolationPolicy) {
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

    override fun list(): MutableList<RegionalViolationPolicy> {
        val out = mutableListOf<RegionalViolationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalViolationPolicyStore(): RegionalViolationPolicyStore {
    return MemoryRegionalViolationPolicyStore(
        mutableMapOf<String, RegionalViolationPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalViolationPolicyStore(
    var inner: RegionalViolationPolicyStore,
    var cache: TtlCache<RegionalViolationPolicy>,
    var hits: Int,
    var misses: Int,
) : RegionalViolationPolicyStore {
    override fun find(code: String): RegionalViolationPolicy? {
        val hit = (cache.get(code) as RegionalViolationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalViolationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalViolationPolicy> = inner.list()
}

fun newCachedRegionalViolationPolicyStore(inner: RegionalViolationPolicyStore, ttl: Int, clock: Clock): RegionalViolationPolicyStore {
    return CachedRegionalViolationPolicyStore(
        inner = inner,
        cache = newTtlCache<RegionalViolationPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional violation policies. Empty fields match all. */
data class RegionalViolationPolicyQuery(
    var source: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalViolationPolicy): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalViolationPolicy>): MutableList<RegionalViolationPolicy> {
        val matched = filter(items) { candidate: RegionalViolationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalViolationPolicies(store: RegionalViolationPolicyStore, query: RegionalViolationPolicyQuery): MutableList<RegionalViolationPolicy> {
    return query.apply(store.list())
}
