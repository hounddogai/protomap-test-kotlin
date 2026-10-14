package com.acme.transport.fleetops.inspection.inspectionmapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict inspection mappings by code. */
interface StrictInspectionMappingStore {
    fun find(code: String): StrictInspectionMapping?
    fun save(item: StrictInspectionMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictInspectionMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictInspectionMappingStore(
    var items: MutableMap<String, StrictInspectionMapping>,
    var order: MutableList<String>,
) : StrictInspectionMappingStore {
    override fun find(code: String): StrictInspectionMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictInspectionMapping) {
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

    override fun list(): MutableList<StrictInspectionMapping> {
        val out = mutableListOf<StrictInspectionMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictInspectionMappingStore(): StrictInspectionMappingStore {
    return MemoryStrictInspectionMappingStore(
        mutableMapOf<String, StrictInspectionMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictInspectionMappingStore(
    var inner: StrictInspectionMappingStore,
    var cache: TtlCache<StrictInspectionMapping>,
    var hits: Int,
    var misses: Int,
) : StrictInspectionMappingStore {
    override fun find(code: String): StrictInspectionMapping? {
        val hit = (cache.get(code) as StrictInspectionMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictInspectionMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictInspectionMapping> = inner.list()
}

fun newCachedStrictInspectionMappingStore(inner: StrictInspectionMappingStore, ttl: Int, clock: Clock): StrictInspectionMappingStore {
    return CachedStrictInspectionMappingStore(
        inner = inner,
        cache = newTtlCache<StrictInspectionMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict inspection mappings. Empty fields match all. */
data class StrictInspectionMappingQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictInspectionMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<StrictInspectionMapping>): MutableList<StrictInspectionMapping> {
        val matched = filter(items) { candidate: StrictInspectionMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictInspectionMappings(store: StrictInspectionMappingStore, query: StrictInspectionMappingQuery): MutableList<StrictInspectionMapping> {
    return query.apply(store.list())
}
