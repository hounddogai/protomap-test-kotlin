package com.acme.transport.customs.inspection.mapping

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default inspection mappings by code. */
interface DefaultInspectionMappingStore {
    fun find(code: String): DefaultInspectionMapping?
    fun save(item: DefaultInspectionMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultInspectionMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultInspectionMappingStore(
    var items: MutableMap<String, DefaultInspectionMapping>,
    var order: MutableList<String>,
) : DefaultInspectionMappingStore {
    override fun find(code: String): DefaultInspectionMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultInspectionMapping) {
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

    override fun list(): MutableList<DefaultInspectionMapping> {
        val out = mutableListOf<DefaultInspectionMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultInspectionMappingStore(): DefaultInspectionMappingStore {
    return MemoryDefaultInspectionMappingStore(
        mutableMapOf<String, DefaultInspectionMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultInspectionMappingStore(
    var inner: DefaultInspectionMappingStore,
    var cache: TtlCache<DefaultInspectionMapping>,
    var hits: Int,
    var misses: Int,
) : DefaultInspectionMappingStore {
    override fun find(code: String): DefaultInspectionMapping? {
        val hit = (cache.get(code) as DefaultInspectionMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultInspectionMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultInspectionMapping> = inner.list()
}

fun newCachedDefaultInspectionMappingStore(inner: DefaultInspectionMappingStore, ttl: Int, clock: Clock): DefaultInspectionMappingStore {
    return CachedDefaultInspectionMappingStore(
        inner = inner,
        cache = newTtlCache<DefaultInspectionMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default inspection mappings. Empty fields match all. */
data class DefaultInspectionMappingQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultInspectionMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultInspectionMapping>): MutableList<DefaultInspectionMapping> {
        val matched = filter(items) { candidate: DefaultInspectionMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultInspectionMappings(store: DefaultInspectionMappingStore, query: DefaultInspectionMappingQuery): MutableList<DefaultInspectionMapping> {
    return query.apply(store.list())
}
