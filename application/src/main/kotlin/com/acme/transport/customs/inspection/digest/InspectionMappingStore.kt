package com.acme.transport.customs.inspection.digest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection mappings by code. */
interface InspectionMappingStore {
    fun find(code: String): InspectionMapping?
    fun save(item: InspectionMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionMappingStore(
    var items: MutableMap<String, InspectionMapping>,
    var order: MutableList<String>,
) : InspectionMappingStore {
    override fun find(code: String): InspectionMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionMapping) {
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

    override fun list(): MutableList<InspectionMapping> {
        val out = mutableListOf<InspectionMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionMappingStore(): InspectionMappingStore {
    return MemoryInspectionMappingStore(mutableMapOf<String, InspectionMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionMappingStore(
    var inner: InspectionMappingStore,
    var cache: TtlCache<InspectionMapping>,
    var hits: Int,
    var misses: Int,
) : InspectionMappingStore {
    override fun find(code: String): InspectionMapping? {
        val hit = (cache.get(code) as InspectionMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionMapping> = inner.list()
}

fun newCachedInspectionMappingStore(inner: InspectionMappingStore, ttl: Int, clock: Clock): InspectionMappingStore {
    return CachedInspectionMappingStore(
        inner = inner,
        cache = newTtlCache<InspectionMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection mappings. Empty fields match all. */
data class InspectionMappingQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionMapping>): MutableList<InspectionMapping> {
        val matched = filter(items) { candidate: InspectionMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionMappings(store: InspectionMappingStore, query: InspectionMappingQuery): MutableList<InspectionMapping> {
    return query.apply(store.list())
}
