package com.acme.transport.fleetops.utilization.audit

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal utilization policies by code. */
interface SeasonalUtilizationPolicyStore {
    fun find(code: String): SeasonalUtilizationPolicy?
    fun save(item: SeasonalUtilizationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalUtilizationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalUtilizationPolicyStore(
    var items: MutableMap<String, SeasonalUtilizationPolicy>,
    var order: MutableList<String>,
) : SeasonalUtilizationPolicyStore {
    override fun find(code: String): SeasonalUtilizationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalUtilizationPolicy) {
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

    override fun list(): MutableList<SeasonalUtilizationPolicy> {
        val out = mutableListOf<SeasonalUtilizationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalUtilizationPolicyStore(): SeasonalUtilizationPolicyStore {
    return MemorySeasonalUtilizationPolicyStore(
        mutableMapOf<String, SeasonalUtilizationPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalUtilizationPolicyStore(
    var inner: SeasonalUtilizationPolicyStore,
    var cache: TtlCache<SeasonalUtilizationPolicy>,
    var hits: Int,
    var misses: Int,
) : SeasonalUtilizationPolicyStore {
    override fun find(code: String): SeasonalUtilizationPolicy? {
        val hit = (cache.get(code) as SeasonalUtilizationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalUtilizationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalUtilizationPolicy> = inner.list()
}

fun newCachedSeasonalUtilizationPolicyStore(inner: SeasonalUtilizationPolicyStore, ttl: Int, clock: Clock): SeasonalUtilizationPolicyStore {
    return CachedSeasonalUtilizationPolicyStore(
        inner = inner,
        cache = newTtlCache<SeasonalUtilizationPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal utilization policies. Empty fields match all. */
data class SeasonalUtilizationPolicyQuery(
    var segment: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalUtilizationPolicy): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalUtilizationPolicy>): MutableList<SeasonalUtilizationPolicy> {
        val matched = filter(items) { candidate: SeasonalUtilizationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalUtilizationPolicies(store: SeasonalUtilizationPolicyStore, query: SeasonalUtilizationPolicyQuery): MutableList<SeasonalUtilizationPolicy> {
    return query.apply(store.list())
}
