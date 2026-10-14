package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional fuel burn targets by code. */
interface RegionalFuelBurnTargetStore {
    fun find(code: String): RegionalFuelBurnTarget?
    fun save(item: RegionalFuelBurnTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalFuelBurnTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalFuelBurnTargetStore(
    var items: MutableMap<String, RegionalFuelBurnTarget>,
    var order: MutableList<String>,
) : RegionalFuelBurnTargetStore {
    override fun find(code: String): RegionalFuelBurnTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalFuelBurnTarget) {
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

    override fun list(): MutableList<RegionalFuelBurnTarget> {
        val out = mutableListOf<RegionalFuelBurnTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalFuelBurnTargetStore(): RegionalFuelBurnTargetStore {
    return MemoryRegionalFuelBurnTargetStore(
        mutableMapOf<String, RegionalFuelBurnTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalFuelBurnTargetStore(
    var inner: RegionalFuelBurnTargetStore,
    var cache: TtlCache<RegionalFuelBurnTarget>,
    var hits: Int,
    var misses: Int,
) : RegionalFuelBurnTargetStore {
    override fun find(code: String): RegionalFuelBurnTarget? {
        val hit = (cache.get(code) as RegionalFuelBurnTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalFuelBurnTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalFuelBurnTarget> = inner.list()
}

fun newCachedRegionalFuelBurnTargetStore(inner: RegionalFuelBurnTargetStore, ttl: Int, clock: Clock): RegionalFuelBurnTargetStore {
    return CachedRegionalFuelBurnTargetStore(
        inner = inner,
        cache = newTtlCache<RegionalFuelBurnTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional fuel burn targets. Empty fields match all. */
data class RegionalFuelBurnTargetQuery(
    var country: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalFuelBurnTarget): Boolean {
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

    fun apply(items: MutableList<RegionalFuelBurnTarget>): MutableList<RegionalFuelBurnTarget> {
        val matched = filter(items) { candidate: RegionalFuelBurnTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalFuelBurnTargets(store: RegionalFuelBurnTargetStore, query: RegionalFuelBurnTargetQuery): MutableList<RegionalFuelBurnTarget> {
    return query.apply(store.list())
}
