package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled proof of delivery policies by code. */
interface ScheduledProofOfDeliveryPolicyStore {
    fun find(code: String): ScheduledProofOfDeliveryPolicy?
    fun save(item: ScheduledProofOfDeliveryPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledProofOfDeliveryPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledProofOfDeliveryPolicyStore(
    var items: MutableMap<String, ScheduledProofOfDeliveryPolicy>,
    var order: MutableList<String>,
) : ScheduledProofOfDeliveryPolicyStore {
    override fun find(code: String): ScheduledProofOfDeliveryPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledProofOfDeliveryPolicy) {
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

    override fun list(): MutableList<ScheduledProofOfDeliveryPolicy> {
        val out = mutableListOf<ScheduledProofOfDeliveryPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledProofOfDeliveryPolicyStore(): ScheduledProofOfDeliveryPolicyStore {
    return MemoryScheduledProofOfDeliveryPolicyStore(
        mutableMapOf<String, ScheduledProofOfDeliveryPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledProofOfDeliveryPolicyStore(
    var inner: ScheduledProofOfDeliveryPolicyStore,
    var cache: TtlCache<ScheduledProofOfDeliveryPolicy>,
    var hits: Int,
    var misses: Int,
) : ScheduledProofOfDeliveryPolicyStore {
    override fun find(code: String): ScheduledProofOfDeliveryPolicy? {
        val hit = (cache.get(code) as ScheduledProofOfDeliveryPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledProofOfDeliveryPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledProofOfDeliveryPolicy> = inner.list()
}

fun newCachedScheduledProofOfDeliveryPolicyStore(inner: ScheduledProofOfDeliveryPolicyStore, ttl: Int, clock: Clock): ScheduledProofOfDeliveryPolicyStore {
    return CachedScheduledProofOfDeliveryPolicyStore(
        inner = inner,
        cache = newTtlCache<ScheduledProofOfDeliveryPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled proof of delivery policies. Empty fields match all. */
data class ScheduledProofOfDeliveryPolicyQuery(
    var category: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledProofOfDeliveryPolicy): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledProofOfDeliveryPolicy>): MutableList<ScheduledProofOfDeliveryPolicy> {
        val matched = filter(items) { candidate: ScheduledProofOfDeliveryPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledProofOfDeliveryPolicies(store: ScheduledProofOfDeliveryPolicyStore, query: ScheduledProofOfDeliveryPolicyQuery): MutableList<ScheduledProofOfDeliveryPolicy> {
    return query.apply(store.list())
}
