package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy service plan mappings by code. */
interface LegacyServicePlanMappingStore {
    fun find(code: String): LegacyServicePlanMapping?
    fun save(item: LegacyServicePlanMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyServicePlanMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyServicePlanMappingStore(
    var items: MutableMap<String, LegacyServicePlanMapping>,
    var order: MutableList<String>,
) : LegacyServicePlanMappingStore {
    override fun find(code: String): LegacyServicePlanMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyServicePlanMapping) {
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

    override fun list(): MutableList<LegacyServicePlanMapping> {
        val out = mutableListOf<LegacyServicePlanMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyServicePlanMappingStore(): LegacyServicePlanMappingStore {
    return MemoryLegacyServicePlanMappingStore(
        mutableMapOf<String, LegacyServicePlanMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyServicePlanMappingStore(
    var inner: LegacyServicePlanMappingStore,
    var cache: TtlCache<LegacyServicePlanMapping>,
    var hits: Int,
    var misses: Int,
) : LegacyServicePlanMappingStore {
    override fun find(code: String): LegacyServicePlanMapping? {
        val hit = (cache.get(code) as LegacyServicePlanMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyServicePlanMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyServicePlanMapping> = inner.list()
}

fun newCachedLegacyServicePlanMappingStore(inner: LegacyServicePlanMappingStore, ttl: Int, clock: Clock): LegacyServicePlanMappingStore {
    return CachedLegacyServicePlanMappingStore(
        inner = inner,
        cache = newTtlCache<LegacyServicePlanMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy service plan mappings. Empty fields match all. */
data class LegacyServicePlanMappingQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyServicePlanMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LegacyServicePlanMapping>): MutableList<LegacyServicePlanMapping> {
        val matched = filter(items) { candidate: LegacyServicePlanMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyServicePlanMappings(store: LegacyServicePlanMappingStore, query: LegacyServicePlanMappingQuery): MutableList<LegacyServicePlanMapping> {
    return query.apply(store.list())
}
