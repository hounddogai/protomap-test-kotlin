package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient inspection mappings by code. */
interface LenientInspectionMappingStore {
    fun find(code: String): LenientInspectionMapping?
    fun save(item: LenientInspectionMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientInspectionMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientInspectionMappingStore(
    var items: MutableMap<String, LenientInspectionMapping>,
    var order: MutableList<String>,
) : LenientInspectionMappingStore {
    override fun find(code: String): LenientInspectionMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientInspectionMapping) {
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

    override fun list(): MutableList<LenientInspectionMapping> {
        val out = mutableListOf<LenientInspectionMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientInspectionMappingStore(): LenientInspectionMappingStore {
    return MemoryLenientInspectionMappingStore(
        mutableMapOf<String, LenientInspectionMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientInspectionMappingStore(
    var inner: LenientInspectionMappingStore,
    var cache: TtlCache<LenientInspectionMapping>,
    var hits: Int,
    var misses: Int,
) : LenientInspectionMappingStore {
    override fun find(code: String): LenientInspectionMapping? {
        val hit = (cache.get(code) as LenientInspectionMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientInspectionMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientInspectionMapping> = inner.list()
}

fun newCachedLenientInspectionMappingStore(inner: LenientInspectionMappingStore, ttl: Int, clock: Clock): LenientInspectionMappingStore {
    return CachedLenientInspectionMappingStore(
        inner = inner,
        cache = newTtlCache<LenientInspectionMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient inspection mappings. Empty fields match all. */
data class LenientInspectionMappingQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientInspectionMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LenientInspectionMapping>): MutableList<LenientInspectionMapping> {
        val matched = filter(items) { candidate: LenientInspectionMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientInspectionMappings(store: LenientInspectionMappingStore, query: LenientInspectionMappingQuery): MutableList<LenientInspectionMapping> {
    return query.apply(store.list())
}
