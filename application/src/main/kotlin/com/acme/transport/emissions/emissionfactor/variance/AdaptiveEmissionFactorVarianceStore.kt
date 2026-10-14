package com.acme.transport.emissions.emissionfactor.variance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive emission factor variances by code. */
interface AdaptiveEmissionFactorVarianceStore {
    fun find(code: String): AdaptiveEmissionFactorVariance?
    fun save(item: AdaptiveEmissionFactorVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveEmissionFactorVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveEmissionFactorVarianceStore(
    var items: MutableMap<String, AdaptiveEmissionFactorVariance>,
    var order: MutableList<String>,
) : AdaptiveEmissionFactorVarianceStore {
    override fun find(code: String): AdaptiveEmissionFactorVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveEmissionFactorVariance) {
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

    override fun list(): MutableList<AdaptiveEmissionFactorVariance> {
        val out = mutableListOf<AdaptiveEmissionFactorVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveEmissionFactorVarianceStore(): AdaptiveEmissionFactorVarianceStore {
    return MemoryAdaptiveEmissionFactorVarianceStore(
        mutableMapOf<String, AdaptiveEmissionFactorVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveEmissionFactorVarianceStore(
    var inner: AdaptiveEmissionFactorVarianceStore,
    var cache: TtlCache<AdaptiveEmissionFactorVariance>,
    var hits: Int,
    var misses: Int,
) : AdaptiveEmissionFactorVarianceStore {
    override fun find(code: String): AdaptiveEmissionFactorVariance? {
        val hit = (cache.get(code) as AdaptiveEmissionFactorVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveEmissionFactorVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveEmissionFactorVariance> = inner.list()
}

fun newCachedAdaptiveEmissionFactorVarianceStore(inner: AdaptiveEmissionFactorVarianceStore, ttl: Int, clock: Clock): AdaptiveEmissionFactorVarianceStore {
    return CachedAdaptiveEmissionFactorVarianceStore(
        inner = inner,
        cache = newTtlCache<AdaptiveEmissionFactorVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive emission factor variances. Empty fields match all. */
data class AdaptiveEmissionFactorVarianceQuery(
    var locale: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveEmissionFactorVariance): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<AdaptiveEmissionFactorVariance>): MutableList<AdaptiveEmissionFactorVariance> {
        val matched = filter(items) { candidate: AdaptiveEmissionFactorVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveEmissionFactorVariances(store: AdaptiveEmissionFactorVarianceStore, query: AdaptiveEmissionFactorVarianceQuery): MutableList<AdaptiveEmissionFactorVariance> {
    return query.apply(store.list())
}
