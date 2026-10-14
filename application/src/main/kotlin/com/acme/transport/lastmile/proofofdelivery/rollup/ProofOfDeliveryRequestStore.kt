package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery requests by code. */
interface ProofOfDeliveryRequestStore {
    fun find(code: String): ProofOfDeliveryRequest?
    fun save(item: ProofOfDeliveryRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryRequestStore(
    var items: MutableMap<String, ProofOfDeliveryRequest>,
    var order: MutableList<String>,
) : ProofOfDeliveryRequestStore {
    override fun find(code: String): ProofOfDeliveryRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryRequest) {
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

    override fun list(): MutableList<ProofOfDeliveryRequest> {
        val out = mutableListOf<ProofOfDeliveryRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryRequestStore(): ProofOfDeliveryRequestStore {
    return MemoryProofOfDeliveryRequestStore(
        mutableMapOf<String, ProofOfDeliveryRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryRequestStore(
    var inner: ProofOfDeliveryRequestStore,
    var cache: TtlCache<ProofOfDeliveryRequest>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryRequestStore {
    override fun find(code: String): ProofOfDeliveryRequest? {
        val hit = (cache.get(code) as ProofOfDeliveryRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryRequest> = inner.list()
}

fun newCachedProofOfDeliveryRequestStore(inner: ProofOfDeliveryRequestStore, ttl: Int, clock: Clock): ProofOfDeliveryRequestStore {
    return CachedProofOfDeliveryRequestStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery requests. Empty fields match all. */
data class ProofOfDeliveryRequestQuery(
    var label: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryRequest>): MutableList<ProofOfDeliveryRequest> {
        val matched = filter(items) { candidate: ProofOfDeliveryRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryRequests(store: ProofOfDeliveryRequestStore, query: ProofOfDeliveryRequestQuery): MutableList<ProofOfDeliveryRequest> {
    return query.apply(store.list())
}
