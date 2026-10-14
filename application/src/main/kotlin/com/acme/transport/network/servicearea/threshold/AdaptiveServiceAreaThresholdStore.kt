package com.acme.transport.network.servicearea.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps adaptive service area thresholds by code. */
interface AdaptiveServiceAreaThresholdStore {
    fun find(code: String): AdaptiveServiceAreaThreshold?
    fun save(item: AdaptiveServiceAreaThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveServiceAreaThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveServiceAreaThresholdStore(
    var items: MutableMap<String, AdaptiveServiceAreaThreshold>,
    var order: MutableList<String>,
) : AdaptiveServiceAreaThresholdStore {
    override fun find(code: String): AdaptiveServiceAreaThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveServiceAreaThreshold) {
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

    override fun list(): MutableList<AdaptiveServiceAreaThreshold> {
        val out = mutableListOf<AdaptiveServiceAreaThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveServiceAreaThresholdStore(): AdaptiveServiceAreaThresholdStore {
    return MemoryAdaptiveServiceAreaThresholdStore(
        mutableMapOf<String, AdaptiveServiceAreaThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveServiceAreaThresholdStore(
    var inner: AdaptiveServiceAreaThresholdStore,
    var cache: TtlCache<AdaptiveServiceAreaThreshold>,
    var hits: Int,
    var misses: Int,
) : AdaptiveServiceAreaThresholdStore {
    override fun find(code: String): AdaptiveServiceAreaThreshold? {
        val hit = (cache.get(code) as AdaptiveServiceAreaThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveServiceAreaThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveServiceAreaThreshold> = inner.list()
}

fun newCachedAdaptiveServiceAreaThresholdStore(inner: AdaptiveServiceAreaThresholdStore, ttl: Int, clock: Clock): AdaptiveServiceAreaThresholdStore {
    return CachedAdaptiveServiceAreaThresholdStore(
        inner = inner,
        cache = newTtlCache<AdaptiveServiceAreaThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive service area thresholds. Empty fields match all. */
data class AdaptiveServiceAreaThresholdQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveServiceAreaThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveServiceAreaThreshold>): MutableList<AdaptiveServiceAreaThreshold> {
        val matched = filter(items) { candidate: AdaptiveServiceAreaThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveServiceAreaThresholds(store: AdaptiveServiceAreaThresholdStore, query: AdaptiveServiceAreaThresholdQuery): MutableList<AdaptiveServiceAreaThreshold> {
    return query.apply(store.list())
}
