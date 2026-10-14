package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted trailer mappings by code. */
interface WeightedTrailerMappingStore {
    fun find(code: String): WeightedTrailerMapping?
    fun save(item: WeightedTrailerMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTrailerMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTrailerMappingStore(
    var items: MutableMap<String, WeightedTrailerMapping>,
    var order: MutableList<String>,
) : WeightedTrailerMappingStore {
    override fun find(code: String): WeightedTrailerMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTrailerMapping) {
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

    override fun list(): MutableList<WeightedTrailerMapping> {
        val out = mutableListOf<WeightedTrailerMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTrailerMappingStore(): WeightedTrailerMappingStore {
    return MemoryWeightedTrailerMappingStore(
        mutableMapOf<String, WeightedTrailerMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTrailerMappingStore(
    var inner: WeightedTrailerMappingStore,
    var cache: TtlCache<WeightedTrailerMapping>,
    var hits: Int,
    var misses: Int,
) : WeightedTrailerMappingStore {
    override fun find(code: String): WeightedTrailerMapping? {
        val hit = (cache.get(code) as WeightedTrailerMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTrailerMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTrailerMapping> = inner.list()
}

fun newCachedWeightedTrailerMappingStore(inner: WeightedTrailerMappingStore, ttl: Int, clock: Clock): WeightedTrailerMappingStore {
    return CachedWeightedTrailerMappingStore(
        inner = inner,
        cache = newTtlCache<WeightedTrailerMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted trailer mappings. Empty fields match all. */
data class WeightedTrailerMappingQuery(
    var lane: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTrailerMapping): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTrailerMapping>): MutableList<WeightedTrailerMapping> {
        val matched = filter(items) { candidate: WeightedTrailerMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTrailerMappings(store: WeightedTrailerMappingStore, query: WeightedTrailerMappingQuery): MutableList<WeightedTrailerMapping> {
    return query.apply(store.list())
}
