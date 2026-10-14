package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor variances by code. */
interface EmissionFactorVarianceStore {
    fun find(code: String): EmissionFactorVariance?
    fun save(item: EmissionFactorVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorVarianceStore(
    var items: MutableMap<String, EmissionFactorVariance>,
    var order: MutableList<String>,
) : EmissionFactorVarianceStore {
    override fun find(code: String): EmissionFactorVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorVariance) {
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

    override fun list(): MutableList<EmissionFactorVariance> {
        val out = mutableListOf<EmissionFactorVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorVarianceStore(): EmissionFactorVarianceStore {
    return MemoryEmissionFactorVarianceStore(
        mutableMapOf<String, EmissionFactorVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorVarianceStore(
    var inner: EmissionFactorVarianceStore,
    var cache: TtlCache<EmissionFactorVariance>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorVarianceStore {
    override fun find(code: String): EmissionFactorVariance? {
        val hit = (cache.get(code) as EmissionFactorVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorVariance> = inner.list()
}

fun newCachedEmissionFactorVarianceStore(inner: EmissionFactorVarianceStore, ttl: Int, clock: Clock): EmissionFactorVarianceStore {
    return CachedEmissionFactorVarianceStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor variances. Empty fields match all. */
data class EmissionFactorVarianceQuery(
    var depot: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorVariance): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorVariance>): MutableList<EmissionFactorVariance> {
        val matched = filter(items) { candidate: EmissionFactorVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorVariances(store: EmissionFactorVarianceStore, query: EmissionFactorVarianceQuery): MutableList<EmissionFactorVariance> {
    return query.apply(store.list())
}
