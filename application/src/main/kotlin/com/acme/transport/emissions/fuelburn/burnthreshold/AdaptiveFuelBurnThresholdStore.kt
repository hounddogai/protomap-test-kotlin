package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive fuel burn thresholds by code. */
interface AdaptiveFuelBurnThresholdStore {
    fun find(code: String): AdaptiveFuelBurnThreshold?
    fun save(item: AdaptiveFuelBurnThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveFuelBurnThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveFuelBurnThresholdStore(
    var items: MutableMap<String, AdaptiveFuelBurnThreshold>,
    var order: MutableList<String>,
) : AdaptiveFuelBurnThresholdStore {
    override fun find(code: String): AdaptiveFuelBurnThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveFuelBurnThreshold) {
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

    override fun list(): MutableList<AdaptiveFuelBurnThreshold> {
        val out = mutableListOf<AdaptiveFuelBurnThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveFuelBurnThresholdStore(): AdaptiveFuelBurnThresholdStore {
    return MemoryAdaptiveFuelBurnThresholdStore(
        mutableMapOf<String, AdaptiveFuelBurnThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveFuelBurnThresholdStore(
    var inner: AdaptiveFuelBurnThresholdStore,
    var cache: TtlCache<AdaptiveFuelBurnThreshold>,
    var hits: Int,
    var misses: Int,
) : AdaptiveFuelBurnThresholdStore {
    override fun find(code: String): AdaptiveFuelBurnThreshold? {
        val hit = (cache.get(code) as AdaptiveFuelBurnThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveFuelBurnThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveFuelBurnThreshold> = inner.list()
}

fun newCachedAdaptiveFuelBurnThresholdStore(inner: AdaptiveFuelBurnThresholdStore, ttl: Int, clock: Clock): AdaptiveFuelBurnThresholdStore {
    return CachedAdaptiveFuelBurnThresholdStore(
        inner = inner,
        cache = newTtlCache<AdaptiveFuelBurnThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive fuel burn thresholds. Empty fields match all. */
data class AdaptiveFuelBurnThresholdQuery(
    var source: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveFuelBurnThreshold): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveFuelBurnThreshold>): MutableList<AdaptiveFuelBurnThreshold> {
        val matched = filter(items) { candidate: AdaptiveFuelBurnThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveFuelBurnThresholds(store: AdaptiveFuelBurnThresholdStore, query: AdaptiveFuelBurnThresholdQuery): MutableList<AdaptiveFuelBurnThreshold> {
    return query.apply(store.list())
}
