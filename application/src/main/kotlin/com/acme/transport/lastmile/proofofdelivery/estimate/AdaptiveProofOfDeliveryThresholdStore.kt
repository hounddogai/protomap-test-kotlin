package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive proof of delivery thresholds by code. */
interface AdaptiveProofOfDeliveryThresholdStore {
    fun find(code: String): AdaptiveProofOfDeliveryThreshold?
    fun save(item: AdaptiveProofOfDeliveryThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveProofOfDeliveryThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveProofOfDeliveryThresholdStore(
    var items: MutableMap<String, AdaptiveProofOfDeliveryThreshold>,
    var order: MutableList<String>,
) : AdaptiveProofOfDeliveryThresholdStore {
    override fun find(code: String): AdaptiveProofOfDeliveryThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveProofOfDeliveryThreshold) {
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

    override fun list(): MutableList<AdaptiveProofOfDeliveryThreshold> {
        val out = mutableListOf<AdaptiveProofOfDeliveryThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveProofOfDeliveryThresholdStore(): AdaptiveProofOfDeliveryThresholdStore {
    return MemoryAdaptiveProofOfDeliveryThresholdStore(
        mutableMapOf<String, AdaptiveProofOfDeliveryThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveProofOfDeliveryThresholdStore(
    var inner: AdaptiveProofOfDeliveryThresholdStore,
    var cache: TtlCache<AdaptiveProofOfDeliveryThreshold>,
    var hits: Int,
    var misses: Int,
) : AdaptiveProofOfDeliveryThresholdStore {
    override fun find(code: String): AdaptiveProofOfDeliveryThreshold? {
        val hit = (cache.get(code) as AdaptiveProofOfDeliveryThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveProofOfDeliveryThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveProofOfDeliveryThreshold> = inner.list()
}

fun newCachedAdaptiveProofOfDeliveryThresholdStore(inner: AdaptiveProofOfDeliveryThresholdStore, ttl: Int, clock: Clock): AdaptiveProofOfDeliveryThresholdStore {
    return CachedAdaptiveProofOfDeliveryThresholdStore(
        inner = inner,
        cache = newTtlCache<AdaptiveProofOfDeliveryThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive proof of delivery thresholds. Empty fields match all. */
data class AdaptiveProofOfDeliveryThresholdQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveProofOfDeliveryThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveProofOfDeliveryThreshold>): MutableList<AdaptiveProofOfDeliveryThreshold> {
        val matched = filter(items) { candidate: AdaptiveProofOfDeliveryThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveProofOfDeliveryThresholds(store: AdaptiveProofOfDeliveryThresholdStore, query: AdaptiveProofOfDeliveryThresholdQuery): MutableList<AdaptiveProofOfDeliveryThreshold> {
    return query.apply(store.list())
}
