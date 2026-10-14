package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional fuel burn thresholds by code. */
interface RegionalFuelBurnThresholdStore {
    fun find(code: String): RegionalFuelBurnThreshold?
    fun save(item: RegionalFuelBurnThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalFuelBurnThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalFuelBurnThresholdStore(
    var items: MutableMap<String, RegionalFuelBurnThreshold>,
    var order: MutableList<String>,
) : RegionalFuelBurnThresholdStore {
    override fun find(code: String): RegionalFuelBurnThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalFuelBurnThreshold) {
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

    override fun list(): MutableList<RegionalFuelBurnThreshold> {
        val out = mutableListOf<RegionalFuelBurnThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalFuelBurnThresholdStore(): RegionalFuelBurnThresholdStore {
    return MemoryRegionalFuelBurnThresholdStore(
        mutableMapOf<String, RegionalFuelBurnThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalFuelBurnThresholdStore(
    var inner: RegionalFuelBurnThresholdStore,
    var cache: TtlCache<RegionalFuelBurnThreshold>,
    var hits: Int,
    var misses: Int,
) : RegionalFuelBurnThresholdStore {
    override fun find(code: String): RegionalFuelBurnThreshold? {
        val hit = (cache.get(code) as RegionalFuelBurnThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalFuelBurnThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalFuelBurnThreshold> = inner.list()
}

fun newCachedRegionalFuelBurnThresholdStore(inner: RegionalFuelBurnThresholdStore, ttl: Int, clock: Clock): RegionalFuelBurnThresholdStore {
    return CachedRegionalFuelBurnThresholdStore(
        inner = inner,
        cache = newTtlCache<RegionalFuelBurnThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional fuel burn thresholds. Empty fields match all. */
data class RegionalFuelBurnThresholdQuery(
    var channel: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalFuelBurnThreshold): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalFuelBurnThreshold>): MutableList<RegionalFuelBurnThreshold> {
        val matched = filter(items) { candidate: RegionalFuelBurnThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalFuelBurnThresholds(store: RegionalFuelBurnThresholdStore, query: RegionalFuelBurnThresholdQuery): MutableList<RegionalFuelBurnThreshold> {
    return query.apply(store.list())
}
