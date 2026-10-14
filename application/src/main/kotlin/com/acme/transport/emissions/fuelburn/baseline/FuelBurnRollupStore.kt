package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn rollups by code. */
interface FuelBurnRollupStore {
    fun find(code: String): FuelBurnRollup?
    fun save(item: FuelBurnRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnRollupStore(
    var items: MutableMap<String, FuelBurnRollup>,
    var order: MutableList<String>,
) : FuelBurnRollupStore {
    override fun find(code: String): FuelBurnRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnRollup) {
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

    override fun list(): MutableList<FuelBurnRollup> {
        val out = mutableListOf<FuelBurnRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnRollupStore(): FuelBurnRollupStore {
    return MemoryFuelBurnRollupStore(mutableMapOf<String, FuelBurnRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnRollupStore(
    var inner: FuelBurnRollupStore,
    var cache: TtlCache<FuelBurnRollup>,
    var hits: Int,
    var misses: Int,
) : FuelBurnRollupStore {
    override fun find(code: String): FuelBurnRollup? {
        val hit = (cache.get(code) as FuelBurnRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnRollup> = inner.list()
}

fun newCachedFuelBurnRollupStore(inner: FuelBurnRollupStore, ttl: Int, clock: Clock): FuelBurnRollupStore {
    return CachedFuelBurnRollupStore(
        inner = inner,
        cache = newTtlCache<FuelBurnRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn rollups. Empty fields match all. */
data class FuelBurnRollupQuery(
    var country: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnRollup): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<FuelBurnRollup>): MutableList<FuelBurnRollup> {
        val matched = filter(items) { candidate: FuelBurnRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnRollups(store: FuelBurnRollupStore, query: FuelBurnRollupQuery): MutableList<FuelBurnRollup> {
    return query.apply(store.list())
}
