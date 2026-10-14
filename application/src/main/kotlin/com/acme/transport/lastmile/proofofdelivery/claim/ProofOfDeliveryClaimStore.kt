package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery claims by code. */
interface ProofOfDeliveryClaimStore {
    fun find(code: String): ProofOfDeliveryClaim?
    fun save(item: ProofOfDeliveryClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryClaimStore(
    var items: MutableMap<String, ProofOfDeliveryClaim>,
    var order: MutableList<String>,
) : ProofOfDeliveryClaimStore {
    override fun find(code: String): ProofOfDeliveryClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryClaim) {
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

    override fun list(): MutableList<ProofOfDeliveryClaim> {
        val out = mutableListOf<ProofOfDeliveryClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryClaimStore(): ProofOfDeliveryClaimStore {
    return MemoryProofOfDeliveryClaimStore(
        mutableMapOf<String, ProofOfDeliveryClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryClaimStore(
    var inner: ProofOfDeliveryClaimStore,
    var cache: TtlCache<ProofOfDeliveryClaim>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryClaimStore {
    override fun find(code: String): ProofOfDeliveryClaim? {
        val hit = (cache.get(code) as ProofOfDeliveryClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryClaim> = inner.list()
}

fun newCachedProofOfDeliveryClaimStore(inner: ProofOfDeliveryClaimStore, ttl: Int, clock: Clock): ProofOfDeliveryClaimStore {
    return CachedProofOfDeliveryClaimStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery claims. Empty fields match all. */
data class ProofOfDeliveryClaimQuery(
    var depot: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryClaim): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryClaim>): MutableList<ProofOfDeliveryClaim> {
        val matched = filter(items) { candidate: ProofOfDeliveryClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryClaims(store: ProofOfDeliveryClaimStore, query: ProofOfDeliveryClaimQuery): MutableList<ProofOfDeliveryClaim> {
    return query.apply(store.list())
}
