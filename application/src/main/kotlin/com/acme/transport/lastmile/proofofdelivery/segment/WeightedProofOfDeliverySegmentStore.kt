package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted proof of delivery segments by code. */
interface WeightedProofOfDeliverySegmentStore {
    fun find(code: String): WeightedProofOfDeliverySegment?
    fun save(item: WeightedProofOfDeliverySegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedProofOfDeliverySegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedProofOfDeliverySegmentStore(
    var items: MutableMap<String, WeightedProofOfDeliverySegment>,
    var order: MutableList<String>,
) : WeightedProofOfDeliverySegmentStore {
    override fun find(code: String): WeightedProofOfDeliverySegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedProofOfDeliverySegment) {
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

    override fun list(): MutableList<WeightedProofOfDeliverySegment> {
        val out = mutableListOf<WeightedProofOfDeliverySegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedProofOfDeliverySegmentStore(): WeightedProofOfDeliverySegmentStore {
    return MemoryWeightedProofOfDeliverySegmentStore(
        mutableMapOf<String, WeightedProofOfDeliverySegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedProofOfDeliverySegmentStore(
    var inner: WeightedProofOfDeliverySegmentStore,
    var cache: TtlCache<WeightedProofOfDeliverySegment>,
    var hits: Int,
    var misses: Int,
) : WeightedProofOfDeliverySegmentStore {
    override fun find(code: String): WeightedProofOfDeliverySegment? {
        val hit = (cache.get(code) as WeightedProofOfDeliverySegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedProofOfDeliverySegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedProofOfDeliverySegment> = inner.list()
}

fun newCachedWeightedProofOfDeliverySegmentStore(inner: WeightedProofOfDeliverySegmentStore, ttl: Int, clock: Clock): WeightedProofOfDeliverySegmentStore {
    return CachedWeightedProofOfDeliverySegmentStore(
        inner = inner,
        cache = newTtlCache<WeightedProofOfDeliverySegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted proof of delivery segments. Empty fields match all. */
data class WeightedProofOfDeliverySegmentQuery(
    var lane: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedProofOfDeliverySegment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedProofOfDeliverySegment>): MutableList<WeightedProofOfDeliverySegment> {
        val matched = filter(items) { candidate: WeightedProofOfDeliverySegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedProofOfDeliverySegments(store: WeightedProofOfDeliverySegmentStore, query: WeightedProofOfDeliverySegmentQuery): MutableList<WeightedProofOfDeliverySegment> {
    return query.apply(store.list())
}
