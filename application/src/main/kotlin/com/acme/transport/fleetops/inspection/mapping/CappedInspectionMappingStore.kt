package com.acme.transport.fleetops.inspection.mapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped inspection mappings by code. */
interface CappedInspectionMappingStore {
    fun find(code: String): CappedInspectionMapping?
    fun save(item: CappedInspectionMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedInspectionMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedInspectionMappingStore(
    var items: MutableMap<String, CappedInspectionMapping>,
    var order: MutableList<String>,
) : CappedInspectionMappingStore {
    override fun find(code: String): CappedInspectionMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedInspectionMapping) {
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

    override fun list(): MutableList<CappedInspectionMapping> {
        val out = mutableListOf<CappedInspectionMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedInspectionMappingStore(): CappedInspectionMappingStore {
    return MemoryCappedInspectionMappingStore(
        mutableMapOf<String, CappedInspectionMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedInspectionMappingStore(
    var inner: CappedInspectionMappingStore,
    var cache: TtlCache<CappedInspectionMapping>,
    var hits: Int,
    var misses: Int,
) : CappedInspectionMappingStore {
    override fun find(code: String): CappedInspectionMapping? {
        val hit = (cache.get(code) as CappedInspectionMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedInspectionMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedInspectionMapping> = inner.list()
}

fun newCachedCappedInspectionMappingStore(inner: CappedInspectionMappingStore, ttl: Int, clock: Clock): CappedInspectionMappingStore {
    return CachedCappedInspectionMappingStore(
        inner = inner,
        cache = newTtlCache<CappedInspectionMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped inspection mappings. Empty fields match all. */
data class CappedInspectionMappingQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedInspectionMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedInspectionMapping>): MutableList<CappedInspectionMapping> {
        val matched = filter(items) { candidate: CappedInspectionMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedInspectionMappings(store: CappedInspectionMappingStore, query: CappedInspectionMappingQuery): MutableList<CappedInspectionMapping> {
    return query.apply(store.list())
}
