package com.acme.transport.emissions.routeoffset.offsetmapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback route offset mappings by code. */
interface FallbackRouteOffsetMappingStore {
    fun find(code: String): FallbackRouteOffsetMapping?
    fun save(item: FallbackRouteOffsetMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRouteOffsetMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRouteOffsetMappingStore(
    var items: MutableMap<String, FallbackRouteOffsetMapping>,
    var order: MutableList<String>,
) : FallbackRouteOffsetMappingStore {
    override fun find(code: String): FallbackRouteOffsetMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRouteOffsetMapping) {
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

    override fun list(): MutableList<FallbackRouteOffsetMapping> {
        val out = mutableListOf<FallbackRouteOffsetMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRouteOffsetMappingStore(): FallbackRouteOffsetMappingStore {
    return MemoryFallbackRouteOffsetMappingStore(
        mutableMapOf<String, FallbackRouteOffsetMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRouteOffsetMappingStore(
    var inner: FallbackRouteOffsetMappingStore,
    var cache: TtlCache<FallbackRouteOffsetMapping>,
    var hits: Int,
    var misses: Int,
) : FallbackRouteOffsetMappingStore {
    override fun find(code: String): FallbackRouteOffsetMapping? {
        val hit = (cache.get(code) as FallbackRouteOffsetMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRouteOffsetMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRouteOffsetMapping> = inner.list()
}

fun newCachedFallbackRouteOffsetMappingStore(inner: FallbackRouteOffsetMappingStore, ttl: Int, clock: Clock): FallbackRouteOffsetMappingStore {
    return CachedFallbackRouteOffsetMappingStore(
        inner = inner,
        cache = newTtlCache<FallbackRouteOffsetMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback route offset mappings. Empty fields match all. */
data class FallbackRouteOffsetMappingQuery(
    var unit: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRouteOffsetMapping): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRouteOffsetMapping>): MutableList<FallbackRouteOffsetMapping> {
        val matched = filter(items) { candidate: FallbackRouteOffsetMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRouteOffsetMappings(store: FallbackRouteOffsetMappingStore, query: FallbackRouteOffsetMappingQuery): MutableList<FallbackRouteOffsetMapping> {
    return query.apply(store.list())
}
