package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery quotas by code. */
interface ProofOfDeliveryQuotaStore {
    fun find(code: String): ProofOfDeliveryQuota?
    fun save(item: ProofOfDeliveryQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryQuotaStore(
    var items: MutableMap<String, ProofOfDeliveryQuota>,
    var order: MutableList<String>,
) : ProofOfDeliveryQuotaStore {
    override fun find(code: String): ProofOfDeliveryQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryQuota) {
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

    override fun list(): MutableList<ProofOfDeliveryQuota> {
        val out = mutableListOf<ProofOfDeliveryQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryQuotaStore(): ProofOfDeliveryQuotaStore {
    return MemoryProofOfDeliveryQuotaStore(
        mutableMapOf<String, ProofOfDeliveryQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryQuotaStore(
    var inner: ProofOfDeliveryQuotaStore,
    var cache: TtlCache<ProofOfDeliveryQuota>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryQuotaStore {
    override fun find(code: String): ProofOfDeliveryQuota? {
        val hit = (cache.get(code) as ProofOfDeliveryQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryQuota> = inner.list()
}

fun newCachedProofOfDeliveryQuotaStore(inner: ProofOfDeliveryQuotaStore, ttl: Int, clock: Clock): ProofOfDeliveryQuotaStore {
    return CachedProofOfDeliveryQuotaStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery quotas. Empty fields match all. */
data class ProofOfDeliveryQuotaQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryQuota): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ProofOfDeliveryQuota>): MutableList<ProofOfDeliveryQuota> {
        val matched = filter(items) { candidate: ProofOfDeliveryQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryQuotas(store: ProofOfDeliveryQuotaStore, query: ProofOfDeliveryQuotaQuery): MutableList<ProofOfDeliveryQuota> {
    return query.apply(store.list())
}
