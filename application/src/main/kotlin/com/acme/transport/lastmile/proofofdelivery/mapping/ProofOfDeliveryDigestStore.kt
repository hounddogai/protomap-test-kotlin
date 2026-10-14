package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery digests by code. */
interface ProofOfDeliveryDigestStore {
    fun find(code: String): ProofOfDeliveryDigest?
    fun save(item: ProofOfDeliveryDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryDigestStore(
    var items: MutableMap<String, ProofOfDeliveryDigest>,
    var order: MutableList<String>,
) : ProofOfDeliveryDigestStore {
    override fun find(code: String): ProofOfDeliveryDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryDigest) {
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

    override fun list(): MutableList<ProofOfDeliveryDigest> {
        val out = mutableListOf<ProofOfDeliveryDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryDigestStore(): ProofOfDeliveryDigestStore {
    return MemoryProofOfDeliveryDigestStore(
        mutableMapOf<String, ProofOfDeliveryDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryDigestStore(
    var inner: ProofOfDeliveryDigestStore,
    var cache: TtlCache<ProofOfDeliveryDigest>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryDigestStore {
    override fun find(code: String): ProofOfDeliveryDigest? {
        val hit = (cache.get(code) as ProofOfDeliveryDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryDigest> = inner.list()
}

fun newCachedProofOfDeliveryDigestStore(inner: ProofOfDeliveryDigestStore, ttl: Int, clock: Clock): ProofOfDeliveryDigestStore {
    return CachedProofOfDeliveryDigestStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery digests. Empty fields match all. */
data class ProofOfDeliveryDigestQuery(
    var category: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryDigest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryDigest>): MutableList<ProofOfDeliveryDigest> {
        val matched = filter(items) { candidate: ProofOfDeliveryDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryDigests(store: ProofOfDeliveryDigestStore, query: ProofOfDeliveryDigestQuery): MutableList<ProofOfDeliveryDigest> {
    return query.apply(store.list())
}
