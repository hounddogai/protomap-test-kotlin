package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive emission factor mappings by code. */
interface AdaptiveEmissionFactorMappingStore {
    fun find(code: String): AdaptiveEmissionFactorMapping?
    fun save(item: AdaptiveEmissionFactorMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveEmissionFactorMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveEmissionFactorMappingStore(
    var items: MutableMap<String, AdaptiveEmissionFactorMapping>,
    var order: MutableList<String>,
) : AdaptiveEmissionFactorMappingStore {
    override fun find(code: String): AdaptiveEmissionFactorMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveEmissionFactorMapping) {
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

    override fun list(): MutableList<AdaptiveEmissionFactorMapping> {
        val out = mutableListOf<AdaptiveEmissionFactorMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveEmissionFactorMappingStore(): AdaptiveEmissionFactorMappingStore {
    return MemoryAdaptiveEmissionFactorMappingStore(
        mutableMapOf<String, AdaptiveEmissionFactorMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveEmissionFactorMappingStore(
    var inner: AdaptiveEmissionFactorMappingStore,
    var cache: TtlCache<AdaptiveEmissionFactorMapping>,
    var hits: Int,
    var misses: Int,
) : AdaptiveEmissionFactorMappingStore {
    override fun find(code: String): AdaptiveEmissionFactorMapping? {
        val hit = (cache.get(code) as AdaptiveEmissionFactorMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveEmissionFactorMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveEmissionFactorMapping> = inner.list()
}

fun newCachedAdaptiveEmissionFactorMappingStore(inner: AdaptiveEmissionFactorMappingStore, ttl: Int, clock: Clock): AdaptiveEmissionFactorMappingStore {
    return CachedAdaptiveEmissionFactorMappingStore(
        inner = inner,
        cache = newTtlCache<AdaptiveEmissionFactorMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive emission factor mappings. Empty fields match all. */
data class AdaptiveEmissionFactorMappingQuery(
    var region: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveEmissionFactorMapping): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveEmissionFactorMapping>): MutableList<AdaptiveEmissionFactorMapping> {
        val matched = filter(items) { candidate: AdaptiveEmissionFactorMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveEmissionFactorMappings(store: AdaptiveEmissionFactorMappingStore, query: AdaptiveEmissionFactorMappingQuery): MutableList<AdaptiveEmissionFactorMapping> {
    return query.apply(store.list())
}
