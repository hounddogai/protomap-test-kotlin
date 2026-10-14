package com.acme.transport.customs.clearance.metric

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance mappings by code. */
interface ClearanceMappingStore {
    fun find(code: String): ClearanceMapping?
    fun save(item: ClearanceMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceMappingStore(
    var items: MutableMap<String, ClearanceMapping>,
    var order: MutableList<String>,
) : ClearanceMappingStore {
    override fun find(code: String): ClearanceMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceMapping) {
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

    override fun list(): MutableList<ClearanceMapping> {
        val out = mutableListOf<ClearanceMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceMappingStore(): ClearanceMappingStore {
    return MemoryClearanceMappingStore(mutableMapOf<String, ClearanceMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceMappingStore(
    var inner: ClearanceMappingStore,
    var cache: TtlCache<ClearanceMapping>,
    var hits: Int,
    var misses: Int,
) : ClearanceMappingStore {
    override fun find(code: String): ClearanceMapping? {
        val hit = (cache.get(code) as ClearanceMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceMapping> = inner.list()
}

fun newCachedClearanceMappingStore(inner: ClearanceMappingStore, ttl: Int, clock: Clock): ClearanceMappingStore {
    return CachedClearanceMappingStore(
        inner = inner,
        cache = newTtlCache<ClearanceMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance mappings. Empty fields match all. */
data class ClearanceMappingQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceMapping>): MutableList<ClearanceMapping> {
        val matched = filter(items) { candidate: ClearanceMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceMappings(store: ClearanceMappingStore, query: ClearanceMappingQuery): MutableList<ClearanceMapping> {
    return query.apply(store.list())
}
