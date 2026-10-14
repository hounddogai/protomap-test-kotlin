package com.acme.transport.crewing.availability.manifest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive availability mappings by code. */
interface AdaptiveAvailabilityMappingStore {
    fun find(code: String): AdaptiveAvailabilityMapping?
    fun save(item: AdaptiveAvailabilityMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAvailabilityMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAvailabilityMappingStore(
    var items: MutableMap<String, AdaptiveAvailabilityMapping>,
    var order: MutableList<String>,
) : AdaptiveAvailabilityMappingStore {
    override fun find(code: String): AdaptiveAvailabilityMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAvailabilityMapping) {
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

    override fun list(): MutableList<AdaptiveAvailabilityMapping> {
        val out = mutableListOf<AdaptiveAvailabilityMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAvailabilityMappingStore(): AdaptiveAvailabilityMappingStore {
    return MemoryAdaptiveAvailabilityMappingStore(
        mutableMapOf<String, AdaptiveAvailabilityMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAvailabilityMappingStore(
    var inner: AdaptiveAvailabilityMappingStore,
    var cache: TtlCache<AdaptiveAvailabilityMapping>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAvailabilityMappingStore {
    override fun find(code: String): AdaptiveAvailabilityMapping? {
        val hit = (cache.get(code) as AdaptiveAvailabilityMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAvailabilityMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAvailabilityMapping> = inner.list()
}

fun newCachedAdaptiveAvailabilityMappingStore(inner: AdaptiveAvailabilityMappingStore, ttl: Int, clock: Clock): AdaptiveAvailabilityMappingStore {
    return CachedAdaptiveAvailabilityMappingStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAvailabilityMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive availability mappings. Empty fields match all. */
data class AdaptiveAvailabilityMappingQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAvailabilityMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAvailabilityMapping>): MutableList<AdaptiveAvailabilityMapping> {
        val matched = filter(items) { candidate: AdaptiveAvailabilityMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAvailabilityMappings(store: AdaptiveAvailabilityMappingStore, query: AdaptiveAvailabilityMappingQuery): MutableList<AdaptiveAvailabilityMapping> {
    return query.apply(store.list())
}
