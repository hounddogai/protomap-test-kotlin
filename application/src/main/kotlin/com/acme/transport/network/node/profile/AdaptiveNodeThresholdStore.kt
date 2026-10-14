package com.acme.transport.network.node.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps adaptive node thresholds by code. */
interface AdaptiveNodeThresholdStore {
    fun find(code: String): AdaptiveNodeThreshold?
    fun save(item: AdaptiveNodeThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveNodeThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveNodeThresholdStore(
    var items: MutableMap<String, AdaptiveNodeThreshold>,
    var order: MutableList<String>,
) : AdaptiveNodeThresholdStore {
    override fun find(code: String): AdaptiveNodeThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveNodeThreshold) {
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

    override fun list(): MutableList<AdaptiveNodeThreshold> {
        val out = mutableListOf<AdaptiveNodeThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveNodeThresholdStore(): AdaptiveNodeThresholdStore {
    return MemoryAdaptiveNodeThresholdStore(
        mutableMapOf<String, AdaptiveNodeThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveNodeThresholdStore(
    var inner: AdaptiveNodeThresholdStore,
    var cache: TtlCache<AdaptiveNodeThreshold>,
    var hits: Int,
    var misses: Int,
) : AdaptiveNodeThresholdStore {
    override fun find(code: String): AdaptiveNodeThreshold? {
        val hit = (cache.get(code) as AdaptiveNodeThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveNodeThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveNodeThreshold> = inner.list()
}

fun newCachedAdaptiveNodeThresholdStore(inner: AdaptiveNodeThresholdStore, ttl: Int, clock: Clock): AdaptiveNodeThresholdStore {
    return CachedAdaptiveNodeThresholdStore(
        inner = inner,
        cache = newTtlCache<AdaptiveNodeThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive node thresholds. Empty fields match all. */
data class AdaptiveNodeThresholdQuery(
    var locale: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveNodeThreshold): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveNodeThreshold>): MutableList<AdaptiveNodeThreshold> {
        val matched = filter(items) { candidate: AdaptiveNodeThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveNodeThresholds(store: AdaptiveNodeThresholdStore, query: AdaptiveNodeThresholdQuery): MutableList<AdaptiveNodeThreshold> {
    return query.apply(store.list())
}
