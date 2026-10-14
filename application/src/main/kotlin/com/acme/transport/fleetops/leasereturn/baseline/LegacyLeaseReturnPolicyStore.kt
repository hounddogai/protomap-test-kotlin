package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy lease return policies by code. */
interface LegacyLeaseReturnPolicyStore {
    fun find(code: String): LegacyLeaseReturnPolicy?
    fun save(item: LegacyLeaseReturnPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyLeaseReturnPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyLeaseReturnPolicyStore(
    var items: MutableMap<String, LegacyLeaseReturnPolicy>,
    var order: MutableList<String>,
) : LegacyLeaseReturnPolicyStore {
    override fun find(code: String): LegacyLeaseReturnPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyLeaseReturnPolicy) {
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

    override fun list(): MutableList<LegacyLeaseReturnPolicy> {
        val out = mutableListOf<LegacyLeaseReturnPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyLeaseReturnPolicyStore(): LegacyLeaseReturnPolicyStore {
    return MemoryLegacyLeaseReturnPolicyStore(
        mutableMapOf<String, LegacyLeaseReturnPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyLeaseReturnPolicyStore(
    var inner: LegacyLeaseReturnPolicyStore,
    var cache: TtlCache<LegacyLeaseReturnPolicy>,
    var hits: Int,
    var misses: Int,
) : LegacyLeaseReturnPolicyStore {
    override fun find(code: String): LegacyLeaseReturnPolicy? {
        val hit = (cache.get(code) as LegacyLeaseReturnPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyLeaseReturnPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyLeaseReturnPolicy> = inner.list()
}

fun newCachedLegacyLeaseReturnPolicyStore(inner: LegacyLeaseReturnPolicyStore, ttl: Int, clock: Clock): LegacyLeaseReturnPolicyStore {
    return CachedLegacyLeaseReturnPolicyStore(
        inner = inner,
        cache = newTtlCache<LegacyLeaseReturnPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy lease return policies. Empty fields match all. */
data class LegacyLeaseReturnPolicyQuery(
    var country: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyLeaseReturnPolicy): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyLeaseReturnPolicy>): MutableList<LegacyLeaseReturnPolicy> {
        val matched = filter(items) { candidate: LegacyLeaseReturnPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyLeaseReturnPolicies(store: LegacyLeaseReturnPolicyStore, query: LegacyLeaseReturnPolicyQuery): MutableList<LegacyLeaseReturnPolicy> {
    return query.apply(store.list())
}
