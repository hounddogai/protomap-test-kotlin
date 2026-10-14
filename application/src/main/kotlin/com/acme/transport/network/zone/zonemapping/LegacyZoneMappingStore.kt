package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps legacy zone mappings by code. */
interface LegacyZoneMappingStore {
    fun find(code: String): LegacyZoneMapping?
    fun save(item: LegacyZoneMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyZoneMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyZoneMappingStore(
    var items: MutableMap<String, LegacyZoneMapping>,
    var order: MutableList<String>,
) : LegacyZoneMappingStore {
    override fun find(code: String): LegacyZoneMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyZoneMapping) {
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

    override fun list(): MutableList<LegacyZoneMapping> {
        val out = mutableListOf<LegacyZoneMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyZoneMappingStore(): LegacyZoneMappingStore {
    return MemoryLegacyZoneMappingStore(mutableMapOf<String, LegacyZoneMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyZoneMappingStore(
    var inner: LegacyZoneMappingStore,
    var cache: TtlCache<LegacyZoneMapping>,
    var hits: Int,
    var misses: Int,
) : LegacyZoneMappingStore {
    override fun find(code: String): LegacyZoneMapping? {
        val hit = (cache.get(code) as LegacyZoneMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyZoneMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyZoneMapping> = inner.list()
}

fun newCachedLegacyZoneMappingStore(inner: LegacyZoneMappingStore, ttl: Int, clock: Clock): LegacyZoneMappingStore {
    return CachedLegacyZoneMappingStore(
        inner = inner,
        cache = newTtlCache<LegacyZoneMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy zone mappings. Empty fields match all. */
data class LegacyZoneMappingQuery(
    var country: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyZoneMapping): Boolean {
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

    fun apply(items: MutableList<LegacyZoneMapping>): MutableList<LegacyZoneMapping> {
        val matched = filter(items) { candidate: LegacyZoneMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyZoneMappings(store: LegacyZoneMappingStore, query: LegacyZoneMappingQuery): MutableList<LegacyZoneMapping> {
    return query.apply(store.list())
}
