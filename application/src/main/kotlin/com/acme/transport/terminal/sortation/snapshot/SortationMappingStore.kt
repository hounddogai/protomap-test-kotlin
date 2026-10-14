package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation mappings by code. */
interface SortationMappingStore {
    fun find(code: String): SortationMapping?
    fun save(item: SortationMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationMappingStore(
    var items: MutableMap<String, SortationMapping>,
    var order: MutableList<String>,
) : SortationMappingStore {
    override fun find(code: String): SortationMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationMapping) {
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

    override fun list(): MutableList<SortationMapping> {
        val out = mutableListOf<SortationMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationMappingStore(): SortationMappingStore {
    return MemorySortationMappingStore(mutableMapOf<String, SortationMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationMappingStore(
    var inner: SortationMappingStore,
    var cache: TtlCache<SortationMapping>,
    var hits: Int,
    var misses: Int,
) : SortationMappingStore {
    override fun find(code: String): SortationMapping? {
        val hit = (cache.get(code) as SortationMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationMapping> = inner.list()
}

fun newCachedSortationMappingStore(inner: SortationMappingStore, ttl: Int, clock: Clock): SortationMappingStore {
    return CachedSortationMappingStore(
        inner = inner,
        cache = newTtlCache<SortationMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation mappings. Empty fields match all. */
data class SortationMappingQuery(
    var unit: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationMapping): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<SortationMapping>): MutableList<SortationMapping> {
        val matched = filter(items) { candidate: SortationMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationMappings(store: SortationMappingStore, query: SortationMappingQuery): MutableList<SortationMapping> {
    return query.apply(store.list())
}
