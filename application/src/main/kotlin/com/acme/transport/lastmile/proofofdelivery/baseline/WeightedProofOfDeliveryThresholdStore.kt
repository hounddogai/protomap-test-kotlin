package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted proof of delivery thresholds by code. */
interface WeightedProofOfDeliveryThresholdStore {
    fun find(code: String): WeightedProofOfDeliveryThreshold?
    fun save(item: WeightedProofOfDeliveryThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedProofOfDeliveryThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedProofOfDeliveryThresholdStore(
    var items: MutableMap<String, WeightedProofOfDeliveryThreshold>,
    var order: MutableList<String>,
) : WeightedProofOfDeliveryThresholdStore {
    override fun find(code: String): WeightedProofOfDeliveryThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedProofOfDeliveryThreshold) {
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

    override fun list(): MutableList<WeightedProofOfDeliveryThreshold> {
        val out = mutableListOf<WeightedProofOfDeliveryThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedProofOfDeliveryThresholdStore(): WeightedProofOfDeliveryThresholdStore {
    return MemoryWeightedProofOfDeliveryThresholdStore(
        mutableMapOf<String, WeightedProofOfDeliveryThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedProofOfDeliveryThresholdStore(
    var inner: WeightedProofOfDeliveryThresholdStore,
    var cache: TtlCache<WeightedProofOfDeliveryThreshold>,
    var hits: Int,
    var misses: Int,
) : WeightedProofOfDeliveryThresholdStore {
    override fun find(code: String): WeightedProofOfDeliveryThreshold? {
        val hit = (cache.get(code) as WeightedProofOfDeliveryThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedProofOfDeliveryThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedProofOfDeliveryThreshold> = inner.list()
}

fun newCachedWeightedProofOfDeliveryThresholdStore(inner: WeightedProofOfDeliveryThresholdStore, ttl: Int, clock: Clock): WeightedProofOfDeliveryThresholdStore {
    return CachedWeightedProofOfDeliveryThresholdStore(
        inner = inner,
        cache = newTtlCache<WeightedProofOfDeliveryThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted proof of delivery thresholds. Empty fields match all. */
data class WeightedProofOfDeliveryThresholdQuery(
    var unit: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedProofOfDeliveryThreshold): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<WeightedProofOfDeliveryThreshold>): MutableList<WeightedProofOfDeliveryThreshold> {
        val matched = filter(items) { candidate: WeightedProofOfDeliveryThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedProofOfDeliveryThresholds(store: WeightedProofOfDeliveryThresholdStore, query: WeightedProofOfDeliveryThresholdQuery): MutableList<WeightedProofOfDeliveryThreshold> {
    return query.apply(store.list())
}
