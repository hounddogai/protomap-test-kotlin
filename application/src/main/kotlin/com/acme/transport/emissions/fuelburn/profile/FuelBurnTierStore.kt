package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn tiers by code. */
interface FuelBurnTierStore {
    fun find(code: String): FuelBurnTier?
    fun save(item: FuelBurnTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnTierStore(
    var items: MutableMap<String, FuelBurnTier>,
    var order: MutableList<String>,
) : FuelBurnTierStore {
    override fun find(code: String): FuelBurnTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnTier) {
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

    override fun list(): MutableList<FuelBurnTier> {
        val out = mutableListOf<FuelBurnTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnTierStore(): FuelBurnTierStore {
    return MemoryFuelBurnTierStore(mutableMapOf<String, FuelBurnTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnTierStore(
    var inner: FuelBurnTierStore,
    var cache: TtlCache<FuelBurnTier>,
    var hits: Int,
    var misses: Int,
) : FuelBurnTierStore {
    override fun find(code: String): FuelBurnTier? {
        val hit = (cache.get(code) as FuelBurnTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnTier> = inner.list()
}

fun newCachedFuelBurnTierStore(inner: FuelBurnTierStore, ttl: Int, clock: Clock): FuelBurnTierStore {
    return CachedFuelBurnTierStore(
        inner = inner,
        cache = newTtlCache<FuelBurnTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn tiers. Empty fields match all. */
data class FuelBurnTierQuery(
    var country: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnTier): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnTier>): MutableList<FuelBurnTier> {
        val matched = filter(items) { candidate: FuelBurnTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnTiers(store: FuelBurnTierStore, query: FuelBurnTierQuery): MutableList<FuelBurnTier> {
    return query.apply(store.list())
}
