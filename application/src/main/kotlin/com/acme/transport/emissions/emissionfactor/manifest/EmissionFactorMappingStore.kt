package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor mappings by code. */
interface EmissionFactorMappingStore {
    fun find(code: String): EmissionFactorMapping?
    fun save(item: EmissionFactorMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorMappingStore(
    var items: MutableMap<String, EmissionFactorMapping>,
    var order: MutableList<String>,
) : EmissionFactorMappingStore {
    override fun find(code: String): EmissionFactorMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorMapping) {
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

    override fun list(): MutableList<EmissionFactorMapping> {
        val out = mutableListOf<EmissionFactorMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorMappingStore(): EmissionFactorMappingStore {
    return MemoryEmissionFactorMappingStore(
        mutableMapOf<String, EmissionFactorMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorMappingStore(
    var inner: EmissionFactorMappingStore,
    var cache: TtlCache<EmissionFactorMapping>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorMappingStore {
    override fun find(code: String): EmissionFactorMapping? {
        val hit = (cache.get(code) as EmissionFactorMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorMapping> = inner.list()
}

fun newCachedEmissionFactorMappingStore(inner: EmissionFactorMappingStore, ttl: Int, clock: Clock): EmissionFactorMappingStore {
    return CachedEmissionFactorMappingStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor mappings. Empty fields match all. */
data class EmissionFactorMappingQuery(
    var lane: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorMapping): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<EmissionFactorMapping>): MutableList<EmissionFactorMapping> {
        val matched = filter(items) { candidate: EmissionFactorMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorMappings(store: EmissionFactorMappingStore, query: EmissionFactorMappingQuery): MutableList<EmissionFactorMapping> {
    return query.apply(store.list())
}
