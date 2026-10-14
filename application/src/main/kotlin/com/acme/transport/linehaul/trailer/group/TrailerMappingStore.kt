package com.acme.transport.linehaul.trailer.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer mappings by code. */
interface TrailerMappingStore {
    fun find(code: String): TrailerMapping?
    fun save(item: TrailerMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerMappingStore(
    var items: MutableMap<String, TrailerMapping>,
    var order: MutableList<String>,
) : TrailerMappingStore {
    override fun find(code: String): TrailerMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerMapping) {
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

    override fun list(): MutableList<TrailerMapping> {
        val out = mutableListOf<TrailerMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerMappingStore(): TrailerMappingStore {
    return MemoryTrailerMappingStore(mutableMapOf<String, TrailerMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerMappingStore(
    var inner: TrailerMappingStore,
    var cache: TtlCache<TrailerMapping>,
    var hits: Int,
    var misses: Int,
) : TrailerMappingStore {
    override fun find(code: String): TrailerMapping? {
        val hit = (cache.get(code) as TrailerMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerMapping> = inner.list()
}

fun newCachedTrailerMappingStore(inner: TrailerMappingStore, ttl: Int, clock: Clock): TrailerMappingStore {
    return CachedTrailerMappingStore(
        inner = inner,
        cache = newTtlCache<TrailerMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer mappings. Empty fields match all. */
data class TrailerMappingQuery(
    var category: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerMapping): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerMapping>): MutableList<TrailerMapping> {
        val matched = filter(items) { candidate: TrailerMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerMappings(store: TrailerMappingStore, query: TrailerMappingQuery): MutableList<TrailerMapping> {
    return query.apply(store.list())
}
