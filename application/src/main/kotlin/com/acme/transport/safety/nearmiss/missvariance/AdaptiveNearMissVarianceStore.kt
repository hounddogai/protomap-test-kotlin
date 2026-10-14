package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive near miss variances by code. */
interface AdaptiveNearMissVarianceStore {
    fun find(code: String): AdaptiveNearMissVariance?
    fun save(item: AdaptiveNearMissVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveNearMissVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveNearMissVarianceStore(
    var items: MutableMap<String, AdaptiveNearMissVariance>,
    var order: MutableList<String>,
) : AdaptiveNearMissVarianceStore {
    override fun find(code: String): AdaptiveNearMissVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveNearMissVariance) {
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

    override fun list(): MutableList<AdaptiveNearMissVariance> {
        val out = mutableListOf<AdaptiveNearMissVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveNearMissVarianceStore(): AdaptiveNearMissVarianceStore {
    return MemoryAdaptiveNearMissVarianceStore(
        mutableMapOf<String, AdaptiveNearMissVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveNearMissVarianceStore(
    var inner: AdaptiveNearMissVarianceStore,
    var cache: TtlCache<AdaptiveNearMissVariance>,
    var hits: Int,
    var misses: Int,
) : AdaptiveNearMissVarianceStore {
    override fun find(code: String): AdaptiveNearMissVariance? {
        val hit = (cache.get(code) as AdaptiveNearMissVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveNearMissVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveNearMissVariance> = inner.list()
}

fun newCachedAdaptiveNearMissVarianceStore(inner: AdaptiveNearMissVarianceStore, ttl: Int, clock: Clock): AdaptiveNearMissVarianceStore {
    return CachedAdaptiveNearMissVarianceStore(
        inner = inner,
        cache = newTtlCache<AdaptiveNearMissVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive near miss variances. Empty fields match all. */
data class AdaptiveNearMissVarianceQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveNearMissVariance): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveNearMissVariance>): MutableList<AdaptiveNearMissVariance> {
        val matched = filter(items) { candidate: AdaptiveNearMissVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveNearMissVariances(store: AdaptiveNearMissVarianceStore, query: AdaptiveNearMissVarianceQuery): MutableList<AdaptiveNearMissVariance> {
    return query.apply(store.list())
}
