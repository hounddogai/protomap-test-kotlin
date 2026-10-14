package com.acme.transport.fleetops.inspection.inspectionscore

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary inspection mappings by code. */
interface PrimaryInspectionMappingStore {
    fun find(code: String): PrimaryInspectionMapping?
    fun save(item: PrimaryInspectionMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryInspectionMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryInspectionMappingStore(
    var items: MutableMap<String, PrimaryInspectionMapping>,
    var order: MutableList<String>,
) : PrimaryInspectionMappingStore {
    override fun find(code: String): PrimaryInspectionMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryInspectionMapping) {
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

    override fun list(): MutableList<PrimaryInspectionMapping> {
        val out = mutableListOf<PrimaryInspectionMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryInspectionMappingStore(): PrimaryInspectionMappingStore {
    return MemoryPrimaryInspectionMappingStore(
        mutableMapOf<String, PrimaryInspectionMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryInspectionMappingStore(
    var inner: PrimaryInspectionMappingStore,
    var cache: TtlCache<PrimaryInspectionMapping>,
    var hits: Int,
    var misses: Int,
) : PrimaryInspectionMappingStore {
    override fun find(code: String): PrimaryInspectionMapping? {
        val hit = (cache.get(code) as PrimaryInspectionMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryInspectionMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryInspectionMapping> = inner.list()
}

fun newCachedPrimaryInspectionMappingStore(inner: PrimaryInspectionMappingStore, ttl: Int, clock: Clock): PrimaryInspectionMappingStore {
    return CachedPrimaryInspectionMappingStore(
        inner = inner,
        cache = newTtlCache<PrimaryInspectionMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary inspection mappings. Empty fields match all. */
data class PrimaryInspectionMappingQuery(
    var segment: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryInspectionMapping): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryInspectionMapping>): MutableList<PrimaryInspectionMapping> {
        val matched = filter(items) { candidate: PrimaryInspectionMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryInspectionMappings(store: PrimaryInspectionMappingStore, query: PrimaryInspectionMappingQuery): MutableList<PrimaryInspectionMapping> {
    return query.apply(store.list())
}
