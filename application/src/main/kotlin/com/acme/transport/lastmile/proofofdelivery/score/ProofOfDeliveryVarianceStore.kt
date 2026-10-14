package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery variances by code. */
interface ProofOfDeliveryVarianceStore {
    fun find(code: String): ProofOfDeliveryVariance?
    fun save(item: ProofOfDeliveryVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryVarianceStore(
    var items: MutableMap<String, ProofOfDeliveryVariance>,
    var order: MutableList<String>,
) : ProofOfDeliveryVarianceStore {
    override fun find(code: String): ProofOfDeliveryVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryVariance) {
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

    override fun list(): MutableList<ProofOfDeliveryVariance> {
        val out = mutableListOf<ProofOfDeliveryVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryVarianceStore(): ProofOfDeliveryVarianceStore {
    return MemoryProofOfDeliveryVarianceStore(
        mutableMapOf<String, ProofOfDeliveryVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryVarianceStore(
    var inner: ProofOfDeliveryVarianceStore,
    var cache: TtlCache<ProofOfDeliveryVariance>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryVarianceStore {
    override fun find(code: String): ProofOfDeliveryVariance? {
        val hit = (cache.get(code) as ProofOfDeliveryVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryVariance> = inner.list()
}

fun newCachedProofOfDeliveryVarianceStore(inner: ProofOfDeliveryVarianceStore, ttl: Int, clock: Clock): ProofOfDeliveryVarianceStore {
    return CachedProofOfDeliveryVarianceStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery variances. Empty fields match all. */
data class ProofOfDeliveryVarianceQuery(
    var lane: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryVariance): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryVariance>): MutableList<ProofOfDeliveryVariance> {
        val matched = filter(items) { candidate: ProofOfDeliveryVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryVariances(store: ProofOfDeliveryVarianceStore, query: ProofOfDeliveryVarianceQuery): MutableList<ProofOfDeliveryVariance> {
    return query.apply(store.list())
}
