package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional lease return policies by code. */
interface RegionalLeaseReturnPolicyStore {
    fun find(code: String): RegionalLeaseReturnPolicy?
    fun save(item: RegionalLeaseReturnPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalLeaseReturnPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalLeaseReturnPolicyStore(
    var items: MutableMap<String, RegionalLeaseReturnPolicy>,
    var order: MutableList<String>,
) : RegionalLeaseReturnPolicyStore {
    override fun find(code: String): RegionalLeaseReturnPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalLeaseReturnPolicy) {
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

    override fun list(): MutableList<RegionalLeaseReturnPolicy> {
        val out = mutableListOf<RegionalLeaseReturnPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalLeaseReturnPolicyStore(): RegionalLeaseReturnPolicyStore {
    return MemoryRegionalLeaseReturnPolicyStore(
        mutableMapOf<String, RegionalLeaseReturnPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalLeaseReturnPolicyStore(
    var inner: RegionalLeaseReturnPolicyStore,
    var cache: TtlCache<RegionalLeaseReturnPolicy>,
    var hits: Int,
    var misses: Int,
) : RegionalLeaseReturnPolicyStore {
    override fun find(code: String): RegionalLeaseReturnPolicy? {
        val hit = (cache.get(code) as RegionalLeaseReturnPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalLeaseReturnPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalLeaseReturnPolicy> = inner.list()
}

fun newCachedRegionalLeaseReturnPolicyStore(inner: RegionalLeaseReturnPolicyStore, ttl: Int, clock: Clock): RegionalLeaseReturnPolicyStore {
    return CachedRegionalLeaseReturnPolicyStore(
        inner = inner,
        cache = newTtlCache<RegionalLeaseReturnPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional lease return policies. Empty fields match all. */
data class RegionalLeaseReturnPolicyQuery(
    var source: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalLeaseReturnPolicy): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<RegionalLeaseReturnPolicy>): MutableList<RegionalLeaseReturnPolicy> {
        val matched = filter(items) { candidate: RegionalLeaseReturnPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalLeaseReturnPolicies(store: RegionalLeaseReturnPolicyStore, query: RegionalLeaseReturnPolicyQuery): MutableList<RegionalLeaseReturnPolicy> {
    return query.apply(store.list())
}
