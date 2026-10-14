package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn templates by code. */
interface FuelBurnTemplateStore {
    fun find(code: String): FuelBurnTemplate?
    fun save(item: FuelBurnTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnTemplateStore(
    var items: MutableMap<String, FuelBurnTemplate>,
    var order: MutableList<String>,
) : FuelBurnTemplateStore {
    override fun find(code: String): FuelBurnTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnTemplate) {
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

    override fun list(): MutableList<FuelBurnTemplate> {
        val out = mutableListOf<FuelBurnTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnTemplateStore(): FuelBurnTemplateStore {
    return MemoryFuelBurnTemplateStore(mutableMapOf<String, FuelBurnTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnTemplateStore(
    var inner: FuelBurnTemplateStore,
    var cache: TtlCache<FuelBurnTemplate>,
    var hits: Int,
    var misses: Int,
) : FuelBurnTemplateStore {
    override fun find(code: String): FuelBurnTemplate? {
        val hit = (cache.get(code) as FuelBurnTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnTemplate> = inner.list()
}

fun newCachedFuelBurnTemplateStore(inner: FuelBurnTemplateStore, ttl: Int, clock: Clock): FuelBurnTemplateStore {
    return CachedFuelBurnTemplateStore(
        inner = inner,
        cache = newTtlCache<FuelBurnTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn templates. Empty fields match all. */
data class FuelBurnTemplateQuery(
    var region: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnTemplate): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnTemplate>): MutableList<FuelBurnTemplate> {
        val matched = filter(items) { candidate: FuelBurnTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnTemplates(store: FuelBurnTemplateStore, query: FuelBurnTemplateQuery): MutableList<FuelBurnTemplate> {
    return query.apply(store.list())
}
