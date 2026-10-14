package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn variances by code. */
interface FuelBurnVarianceStore {
    fun find(code: String): FuelBurnVariance?
    fun save(item: FuelBurnVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnVarianceStore(
    var items: MutableMap<String, FuelBurnVariance>,
    var order: MutableList<String>,
) : FuelBurnVarianceStore {
    override fun find(code: String): FuelBurnVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnVariance) {
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

    override fun list(): MutableList<FuelBurnVariance> {
        val out = mutableListOf<FuelBurnVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnVarianceStore(): FuelBurnVarianceStore {
    return MemoryFuelBurnVarianceStore(mutableMapOf<String, FuelBurnVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnVarianceStore(
    var inner: FuelBurnVarianceStore,
    var cache: TtlCache<FuelBurnVariance>,
    var hits: Int,
    var misses: Int,
) : FuelBurnVarianceStore {
    override fun find(code: String): FuelBurnVariance? {
        val hit = (cache.get(code) as FuelBurnVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnVariance> = inner.list()
}

fun newCachedFuelBurnVarianceStore(inner: FuelBurnVarianceStore, ttl: Int, clock: Clock): FuelBurnVarianceStore {
    return CachedFuelBurnVarianceStore(
        inner = inner,
        cache = newTtlCache<FuelBurnVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn variances. Empty fields match all. */
data class FuelBurnVarianceQuery(
    var locale: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnVariance): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnVariance>): MutableList<FuelBurnVariance> {
        val matched = filter(items) { candidate: FuelBurnVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnVariances(store: FuelBurnVarianceStore, query: FuelBurnVarianceQuery): MutableList<FuelBurnVariance> {
    return query.apply(store.list())
}
