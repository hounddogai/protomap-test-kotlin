package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident mappings by code. */
interface IncidentMappingStore {
    fun find(code: String): IncidentMapping?
    fun save(item: IncidentMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentMappingStore(
    var items: MutableMap<String, IncidentMapping>,
    var order: MutableList<String>,
) : IncidentMappingStore {
    override fun find(code: String): IncidentMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentMapping) {
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

    override fun list(): MutableList<IncidentMapping> {
        val out = mutableListOf<IncidentMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentMappingStore(): IncidentMappingStore {
    return MemoryIncidentMappingStore(mutableMapOf<String, IncidentMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentMappingStore(
    var inner: IncidentMappingStore,
    var cache: TtlCache<IncidentMapping>,
    var hits: Int,
    var misses: Int,
) : IncidentMappingStore {
    override fun find(code: String): IncidentMapping? {
        val hit = (cache.get(code) as IncidentMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentMapping> = inner.list()
}

fun newCachedIncidentMappingStore(inner: IncidentMappingStore, ttl: Int, clock: Clock): IncidentMappingStore {
    return CachedIncidentMappingStore(
        inner = inner,
        cache = newTtlCache<IncidentMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident mappings. Empty fields match all. */
data class IncidentMappingQuery(
    var country: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentMapping): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<IncidentMapping>): MutableList<IncidentMapping> {
        val matched = filter(items) { candidate: IncidentMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentMappings(store: IncidentMappingStore, query: IncidentMappingQuery): MutableList<IncidentMapping> {
    return query.apply(store.list())
}
