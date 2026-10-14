package com.acme.transport.fleetops.defect.mapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect mappings by code. */
interface DefectMappingStore {
    fun find(code: String): DefectMapping?
    fun save(item: DefectMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectMappingStore(
    var items: MutableMap<String, DefectMapping>,
    var order: MutableList<String>,
) : DefectMappingStore {
    override fun find(code: String): DefectMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectMapping) {
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

    override fun list(): MutableList<DefectMapping> {
        val out = mutableListOf<DefectMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectMappingStore(): DefectMappingStore {
    return MemoryDefectMappingStore(mutableMapOf<String, DefectMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectMappingStore(
    var inner: DefectMappingStore,
    var cache: TtlCache<DefectMapping>,
    var hits: Int,
    var misses: Int,
) : DefectMappingStore {
    override fun find(code: String): DefectMapping? {
        val hit = (cache.get(code) as DefectMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectMapping> = inner.list()
}

fun newCachedDefectMappingStore(inner: DefectMappingStore, ttl: Int, clock: Clock): DefectMappingStore {
    return CachedDefectMappingStore(
        inner = inner,
        cache = newTtlCache<DefectMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect mappings. Empty fields match all. */
data class DefectMappingQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DefectMapping>): MutableList<DefectMapping> {
        val matched = filter(items) { candidate: DefectMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectMappings(store: DefectMappingStore, query: DefectMappingQuery): MutableList<DefectMapping> {
    return query.apply(store.list())
}
