package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery segments by code. */
interface ProofOfDeliverySegmentStore {
    fun find(code: String): ProofOfDeliverySegment?
    fun save(item: ProofOfDeliverySegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliverySegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliverySegmentStore(
    var items: MutableMap<String, ProofOfDeliverySegment>,
    var order: MutableList<String>,
) : ProofOfDeliverySegmentStore {
    override fun find(code: String): ProofOfDeliverySegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliverySegment) {
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

    override fun list(): MutableList<ProofOfDeliverySegment> {
        val out = mutableListOf<ProofOfDeliverySegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliverySegmentStore(): ProofOfDeliverySegmentStore {
    return MemoryProofOfDeliverySegmentStore(
        mutableMapOf<String, ProofOfDeliverySegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliverySegmentStore(
    var inner: ProofOfDeliverySegmentStore,
    var cache: TtlCache<ProofOfDeliverySegment>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliverySegmentStore {
    override fun find(code: String): ProofOfDeliverySegment? {
        val hit = (cache.get(code) as ProofOfDeliverySegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliverySegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliverySegment> = inner.list()
}

fun newCachedProofOfDeliverySegmentStore(inner: ProofOfDeliverySegmentStore, ttl: Int, clock: Clock): ProofOfDeliverySegmentStore {
    return CachedProofOfDeliverySegmentStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliverySegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery segments. Empty fields match all. */
data class ProofOfDeliverySegmentQuery(
    var channel: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliverySegment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliverySegment>): MutableList<ProofOfDeliverySegment> {
        val matched = filter(items) { candidate: ProofOfDeliverySegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliverySegments(store: ProofOfDeliverySegmentStore, query: ProofOfDeliverySegmentQuery): MutableList<ProofOfDeliverySegment> {
    return query.apply(store.list())
}
