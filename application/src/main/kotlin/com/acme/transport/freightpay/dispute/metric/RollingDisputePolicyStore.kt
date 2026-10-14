package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling dispute policies by code. */
interface RollingDisputePolicyStore {
    fun find(code: String): RollingDisputePolicy?
    fun save(item: RollingDisputePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDisputePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDisputePolicyStore(
    var items: MutableMap<String, RollingDisputePolicy>,
    var order: MutableList<String>,
) : RollingDisputePolicyStore {
    override fun find(code: String): RollingDisputePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDisputePolicy) {
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

    override fun list(): MutableList<RollingDisputePolicy> {
        val out = mutableListOf<RollingDisputePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDisputePolicyStore(): RollingDisputePolicyStore {
    return MemoryRollingDisputePolicyStore(
        mutableMapOf<String, RollingDisputePolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDisputePolicyStore(
    var inner: RollingDisputePolicyStore,
    var cache: TtlCache<RollingDisputePolicy>,
    var hits: Int,
    var misses: Int,
) : RollingDisputePolicyStore {
    override fun find(code: String): RollingDisputePolicy? {
        val hit = (cache.get(code) as RollingDisputePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDisputePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDisputePolicy> = inner.list()
}

fun newCachedRollingDisputePolicyStore(inner: RollingDisputePolicyStore, ttl: Int, clock: Clock): RollingDisputePolicyStore {
    return CachedRollingDisputePolicyStore(
        inner = inner,
        cache = newTtlCache<RollingDisputePolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling dispute policies. Empty fields match all. */
data class RollingDisputePolicyQuery(
    var country: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDisputePolicy): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDisputePolicy>): MutableList<RollingDisputePolicy> {
        val matched = filter(items) { candidate: RollingDisputePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDisputePolicies(store: RollingDisputePolicyStore, query: RollingDisputePolicyQuery): MutableList<RollingDisputePolicy> {
    return query.apply(store.list())
}
