package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive trailer yard mappings by code. */
interface AdaptiveTrailerYardMappingStore {
    fun find(code: String): AdaptiveTrailerYardMapping?
    fun save(item: AdaptiveTrailerYardMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTrailerYardMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTrailerYardMappingStore(
    var items: MutableMap<String, AdaptiveTrailerYardMapping>,
    var order: MutableList<String>,
) : AdaptiveTrailerYardMappingStore {
    override fun find(code: String): AdaptiveTrailerYardMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTrailerYardMapping) {
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

    override fun list(): MutableList<AdaptiveTrailerYardMapping> {
        val out = mutableListOf<AdaptiveTrailerYardMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTrailerYardMappingStore(): AdaptiveTrailerYardMappingStore {
    return MemoryAdaptiveTrailerYardMappingStore(
        mutableMapOf<String, AdaptiveTrailerYardMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTrailerYardMappingStore(
    var inner: AdaptiveTrailerYardMappingStore,
    var cache: TtlCache<AdaptiveTrailerYardMapping>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTrailerYardMappingStore {
    override fun find(code: String): AdaptiveTrailerYardMapping? {
        val hit = (cache.get(code) as AdaptiveTrailerYardMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTrailerYardMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTrailerYardMapping> = inner.list()
}

fun newCachedAdaptiveTrailerYardMappingStore(inner: AdaptiveTrailerYardMappingStore, ttl: Int, clock: Clock): AdaptiveTrailerYardMappingStore {
    return CachedAdaptiveTrailerYardMappingStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTrailerYardMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive trailer yard mappings. Empty fields match all. */
data class AdaptiveTrailerYardMappingQuery(
    var lane: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTrailerYardMapping): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTrailerYardMapping>): MutableList<AdaptiveTrailerYardMapping> {
        val matched = filter(items) { candidate: AdaptiveTrailerYardMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTrailerYardMappings(store: AdaptiveTrailerYardMappingStore, query: AdaptiveTrailerYardMappingQuery): MutableList<AdaptiveTrailerYardMapping> {
    return query.apply(store.list())
}
