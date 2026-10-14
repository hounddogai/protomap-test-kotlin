package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual proof of delivery claims by code. */
interface ManualProofOfDeliveryClaimStore {
    fun find(code: String): ManualProofOfDeliveryClaim?
    fun save(item: ManualProofOfDeliveryClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualProofOfDeliveryClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualProofOfDeliveryClaimStore(
    var items: MutableMap<String, ManualProofOfDeliveryClaim>,
    var order: MutableList<String>,
) : ManualProofOfDeliveryClaimStore {
    override fun find(code: String): ManualProofOfDeliveryClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualProofOfDeliveryClaim) {
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

    override fun list(): MutableList<ManualProofOfDeliveryClaim> {
        val out = mutableListOf<ManualProofOfDeliveryClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualProofOfDeliveryClaimStore(): ManualProofOfDeliveryClaimStore {
    return MemoryManualProofOfDeliveryClaimStore(
        mutableMapOf<String, ManualProofOfDeliveryClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualProofOfDeliveryClaimStore(
    var inner: ManualProofOfDeliveryClaimStore,
    var cache: TtlCache<ManualProofOfDeliveryClaim>,
    var hits: Int,
    var misses: Int,
) : ManualProofOfDeliveryClaimStore {
    override fun find(code: String): ManualProofOfDeliveryClaim? {
        val hit = (cache.get(code) as ManualProofOfDeliveryClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualProofOfDeliveryClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualProofOfDeliveryClaim> = inner.list()
}

fun newCachedManualProofOfDeliveryClaimStore(inner: ManualProofOfDeliveryClaimStore, ttl: Int, clock: Clock): ManualProofOfDeliveryClaimStore {
    return CachedManualProofOfDeliveryClaimStore(
        inner = inner,
        cache = newTtlCache<ManualProofOfDeliveryClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual proof of delivery claims. Empty fields match all. */
data class ManualProofOfDeliveryClaimQuery(
    var depot: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualProofOfDeliveryClaim): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualProofOfDeliveryClaim>): MutableList<ManualProofOfDeliveryClaim> {
        val matched = filter(items) { candidate: ManualProofOfDeliveryClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualProofOfDeliveryClaims(store: ManualProofOfDeliveryClaimStore, query: ManualProofOfDeliveryClaimQuery): MutableList<ManualProofOfDeliveryClaim> {
    return query.apply(store.list())
}
