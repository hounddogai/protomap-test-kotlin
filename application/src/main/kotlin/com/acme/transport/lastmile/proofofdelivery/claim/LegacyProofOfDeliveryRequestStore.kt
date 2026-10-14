package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy proof of delivery requests by code. */
interface LegacyProofOfDeliveryRequestStore {
    fun find(code: String): LegacyProofOfDeliveryRequest?
    fun save(item: LegacyProofOfDeliveryRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyProofOfDeliveryRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyProofOfDeliveryRequestStore(
    var items: MutableMap<String, LegacyProofOfDeliveryRequest>,
    var order: MutableList<String>,
) : LegacyProofOfDeliveryRequestStore {
    override fun find(code: String): LegacyProofOfDeliveryRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyProofOfDeliveryRequest) {
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

    override fun list(): MutableList<LegacyProofOfDeliveryRequest> {
        val out = mutableListOf<LegacyProofOfDeliveryRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyProofOfDeliveryRequestStore(): LegacyProofOfDeliveryRequestStore {
    return MemoryLegacyProofOfDeliveryRequestStore(
        mutableMapOf<String, LegacyProofOfDeliveryRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyProofOfDeliveryRequestStore(
    var inner: LegacyProofOfDeliveryRequestStore,
    var cache: TtlCache<LegacyProofOfDeliveryRequest>,
    var hits: Int,
    var misses: Int,
) : LegacyProofOfDeliveryRequestStore {
    override fun find(code: String): LegacyProofOfDeliveryRequest? {
        val hit = (cache.get(code) as LegacyProofOfDeliveryRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyProofOfDeliveryRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyProofOfDeliveryRequest> = inner.list()
}

fun newCachedLegacyProofOfDeliveryRequestStore(inner: LegacyProofOfDeliveryRequestStore, ttl: Int, clock: Clock): LegacyProofOfDeliveryRequestStore {
    return CachedLegacyProofOfDeliveryRequestStore(
        inner = inner,
        cache = newTtlCache<LegacyProofOfDeliveryRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy proof of delivery requests. Empty fields match all. */
data class LegacyProofOfDeliveryRequestQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyProofOfDeliveryRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyProofOfDeliveryRequest>): MutableList<LegacyProofOfDeliveryRequest> {
        val matched = filter(items) { candidate: LegacyProofOfDeliveryRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyProofOfDeliveryRequests(store: LegacyProofOfDeliveryRequestStore, query: LegacyProofOfDeliveryRequestQuery): MutableList<LegacyProofOfDeliveryRequest> {
    return query.apply(store.list())
}
