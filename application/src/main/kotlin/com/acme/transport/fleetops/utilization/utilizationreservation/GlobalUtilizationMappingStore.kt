package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global utilization mappings by code. */
interface GlobalUtilizationMappingStore {
    fun find(code: String): GlobalUtilizationMapping?
    fun save(item: GlobalUtilizationMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalUtilizationMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalUtilizationMappingStore(
    var items: MutableMap<String, GlobalUtilizationMapping>,
    var order: MutableList<String>,
) : GlobalUtilizationMappingStore {
    override fun find(code: String): GlobalUtilizationMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalUtilizationMapping) {
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

    override fun list(): MutableList<GlobalUtilizationMapping> {
        val out = mutableListOf<GlobalUtilizationMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalUtilizationMappingStore(): GlobalUtilizationMappingStore {
    return MemoryGlobalUtilizationMappingStore(
        mutableMapOf<String, GlobalUtilizationMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalUtilizationMappingStore(
    var inner: GlobalUtilizationMappingStore,
    var cache: TtlCache<GlobalUtilizationMapping>,
    var hits: Int,
    var misses: Int,
) : GlobalUtilizationMappingStore {
    override fun find(code: String): GlobalUtilizationMapping? {
        val hit = (cache.get(code) as GlobalUtilizationMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalUtilizationMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalUtilizationMapping> = inner.list()
}

fun newCachedGlobalUtilizationMappingStore(inner: GlobalUtilizationMappingStore, ttl: Int, clock: Clock): GlobalUtilizationMappingStore {
    return CachedGlobalUtilizationMappingStore(
        inner = inner,
        cache = newTtlCache<GlobalUtilizationMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global utilization mappings. Empty fields match all. */
data class GlobalUtilizationMappingQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalUtilizationMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalUtilizationMapping>): MutableList<GlobalUtilizationMapping> {
        val matched = filter(items) { candidate: GlobalUtilizationMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalUtilizationMappings(store: GlobalUtilizationMappingStore, query: GlobalUtilizationMappingQuery): MutableList<GlobalUtilizationMapping> {
    return query.apply(store.list())
}
