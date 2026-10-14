package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict emission factor mappings by code. */
interface StrictEmissionFactorMappingStore {
    fun find(code: String): StrictEmissionFactorMapping?
    fun save(item: StrictEmissionFactorMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictEmissionFactorMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictEmissionFactorMappingStore(
    var items: MutableMap<String, StrictEmissionFactorMapping>,
    var order: MutableList<String>,
) : StrictEmissionFactorMappingStore {
    override fun find(code: String): StrictEmissionFactorMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictEmissionFactorMapping) {
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

    override fun list(): MutableList<StrictEmissionFactorMapping> {
        val out = mutableListOf<StrictEmissionFactorMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictEmissionFactorMappingStore(): StrictEmissionFactorMappingStore {
    return MemoryStrictEmissionFactorMappingStore(
        mutableMapOf<String, StrictEmissionFactorMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictEmissionFactorMappingStore(
    var inner: StrictEmissionFactorMappingStore,
    var cache: TtlCache<StrictEmissionFactorMapping>,
    var hits: Int,
    var misses: Int,
) : StrictEmissionFactorMappingStore {
    override fun find(code: String): StrictEmissionFactorMapping? {
        val hit = (cache.get(code) as StrictEmissionFactorMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictEmissionFactorMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictEmissionFactorMapping> = inner.list()
}

fun newCachedStrictEmissionFactorMappingStore(inner: StrictEmissionFactorMappingStore, ttl: Int, clock: Clock): StrictEmissionFactorMappingStore {
    return CachedStrictEmissionFactorMappingStore(
        inner = inner,
        cache = newTtlCache<StrictEmissionFactorMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict emission factor mappings. Empty fields match all. */
data class StrictEmissionFactorMappingQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictEmissionFactorMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<StrictEmissionFactorMapping>): MutableList<StrictEmissionFactorMapping> {
        val matched = filter(items) { candidate: StrictEmissionFactorMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictEmissionFactorMappings(store: StrictEmissionFactorMappingStore, query: StrictEmissionFactorMappingQuery): MutableList<StrictEmissionFactorMapping> {
    return query.apply(store.list())
}
