package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global inspection mappings by code. */
interface GlobalInspectionMappingStore {
    fun find(code: String): GlobalInspectionMapping?
    fun save(item: GlobalInspectionMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalInspectionMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalInspectionMappingStore(
    var items: MutableMap<String, GlobalInspectionMapping>,
    var order: MutableList<String>,
) : GlobalInspectionMappingStore {
    override fun find(code: String): GlobalInspectionMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalInspectionMapping) {
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

    override fun list(): MutableList<GlobalInspectionMapping> {
        val out = mutableListOf<GlobalInspectionMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalInspectionMappingStore(): GlobalInspectionMappingStore {
    return MemoryGlobalInspectionMappingStore(
        mutableMapOf<String, GlobalInspectionMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalInspectionMappingStore(
    var inner: GlobalInspectionMappingStore,
    var cache: TtlCache<GlobalInspectionMapping>,
    var hits: Int,
    var misses: Int,
) : GlobalInspectionMappingStore {
    override fun find(code: String): GlobalInspectionMapping? {
        val hit = (cache.get(code) as GlobalInspectionMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalInspectionMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalInspectionMapping> = inner.list()
}

fun newCachedGlobalInspectionMappingStore(inner: GlobalInspectionMappingStore, ttl: Int, clock: Clock): GlobalInspectionMappingStore {
    return CachedGlobalInspectionMappingStore(
        inner = inner,
        cache = newTtlCache<GlobalInspectionMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global inspection mappings. Empty fields match all. */
data class GlobalInspectionMappingQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalInspectionMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalInspectionMapping>): MutableList<GlobalInspectionMapping> {
        val matched = filter(items) { candidate: GlobalInspectionMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalInspectionMappings(store: GlobalInspectionMappingStore, query: GlobalInspectionMappingQuery): MutableList<GlobalInspectionMapping> {
    return query.apply(store.list())
}
