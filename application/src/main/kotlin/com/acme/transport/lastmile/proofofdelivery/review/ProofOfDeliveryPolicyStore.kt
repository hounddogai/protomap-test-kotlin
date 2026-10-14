package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery policies by code. */
interface ProofOfDeliveryPolicyStore {
    fun find(code: String): ProofOfDeliveryPolicy?
    fun save(item: ProofOfDeliveryPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryPolicyStore(
    var items: MutableMap<String, ProofOfDeliveryPolicy>,
    var order: MutableList<String>,
) : ProofOfDeliveryPolicyStore {
    override fun find(code: String): ProofOfDeliveryPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryPolicy) {
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

    override fun list(): MutableList<ProofOfDeliveryPolicy> {
        val out = mutableListOf<ProofOfDeliveryPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryPolicyStore(): ProofOfDeliveryPolicyStore {
    return MemoryProofOfDeliveryPolicyStore(
        mutableMapOf<String, ProofOfDeliveryPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryPolicyStore(
    var inner: ProofOfDeliveryPolicyStore,
    var cache: TtlCache<ProofOfDeliveryPolicy>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryPolicyStore {
    override fun find(code: String): ProofOfDeliveryPolicy? {
        val hit = (cache.get(code) as ProofOfDeliveryPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryPolicy> = inner.list()
}

fun newCachedProofOfDeliveryPolicyStore(inner: ProofOfDeliveryPolicyStore, ttl: Int, clock: Clock): ProofOfDeliveryPolicyStore {
    return CachedProofOfDeliveryPolicyStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery policies. Empty fields match all. */
data class ProofOfDeliveryPolicyQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryPolicy>): MutableList<ProofOfDeliveryPolicy> {
        val matched = filter(items) { candidate: ProofOfDeliveryPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryPolicies(store: ProofOfDeliveryPolicyStore, query: ProofOfDeliveryPolicyQuery): MutableList<ProofOfDeliveryPolicy> {
    return query.apply(store.list())
}
