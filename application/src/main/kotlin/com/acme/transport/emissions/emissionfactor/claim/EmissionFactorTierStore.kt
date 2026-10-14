package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor tiers by code. */
interface EmissionFactorTierStore {
    fun find(code: String): EmissionFactorTier?
    fun save(item: EmissionFactorTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorTierStore(
    var items: MutableMap<String, EmissionFactorTier>,
    var order: MutableList<String>,
) : EmissionFactorTierStore {
    override fun find(code: String): EmissionFactorTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorTier) {
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

    override fun list(): MutableList<EmissionFactorTier> {
        val out = mutableListOf<EmissionFactorTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorTierStore(): EmissionFactorTierStore {
    return MemoryEmissionFactorTierStore(mutableMapOf<String, EmissionFactorTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorTierStore(
    var inner: EmissionFactorTierStore,
    var cache: TtlCache<EmissionFactorTier>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorTierStore {
    override fun find(code: String): EmissionFactorTier? {
        val hit = (cache.get(code) as EmissionFactorTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorTier> = inner.list()
}

fun newCachedEmissionFactorTierStore(inner: EmissionFactorTierStore, ttl: Int, clock: Clock): EmissionFactorTierStore {
    return CachedEmissionFactorTierStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor tiers. Empty fields match all. */
data class EmissionFactorTierQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<EmissionFactorTier>): MutableList<EmissionFactorTier> {
        val matched = filter(items) { candidate: EmissionFactorTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorTiers(store: EmissionFactorTierStore, query: EmissionFactorTierQuery): MutableList<EmissionFactorTier> {
    return query.apply(store.list())
}
