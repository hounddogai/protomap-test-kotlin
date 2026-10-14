package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute policies by code. */
interface DisputePolicyStore {
    fun find(code: String): DisputePolicy?
    fun save(item: DisputePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputePolicyStore(
    var items: MutableMap<String, DisputePolicy>,
    var order: MutableList<String>,
) : DisputePolicyStore {
    override fun find(code: String): DisputePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputePolicy) {
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

    override fun list(): MutableList<DisputePolicy> {
        val out = mutableListOf<DisputePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputePolicyStore(): DisputePolicyStore {
    return MemoryDisputePolicyStore(mutableMapOf<String, DisputePolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputePolicyStore(
    var inner: DisputePolicyStore,
    var cache: TtlCache<DisputePolicy>,
    var hits: Int,
    var misses: Int,
) : DisputePolicyStore {
    override fun find(code: String): DisputePolicy? {
        val hit = (cache.get(code) as DisputePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputePolicy> = inner.list()
}

fun newCachedDisputePolicyStore(inner: DisputePolicyStore, ttl: Int, clock: Clock): DisputePolicyStore {
    return CachedDisputePolicyStore(
        inner = inner,
        cache = newTtlCache<DisputePolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute policies. Empty fields match all. */
data class DisputePolicyQuery(
    var country: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputePolicy): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputePolicy>): MutableList<DisputePolicy> {
        val matched = filter(items) { candidate: DisputePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputePolicies(store: DisputePolicyStore, query: DisputePolicyQuery): MutableList<DisputePolicy> {
    return query.apply(store.list())
}
