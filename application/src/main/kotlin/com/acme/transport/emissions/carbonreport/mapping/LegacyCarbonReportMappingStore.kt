package com.acme.transport.emissions.carbonreport.mapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy carbon report mappings by code. */
interface LegacyCarbonReportMappingStore {
    fun find(code: String): LegacyCarbonReportMapping?
    fun save(item: LegacyCarbonReportMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCarbonReportMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCarbonReportMappingStore(
    var items: MutableMap<String, LegacyCarbonReportMapping>,
    var order: MutableList<String>,
) : LegacyCarbonReportMappingStore {
    override fun find(code: String): LegacyCarbonReportMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCarbonReportMapping) {
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

    override fun list(): MutableList<LegacyCarbonReportMapping> {
        val out = mutableListOf<LegacyCarbonReportMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCarbonReportMappingStore(): LegacyCarbonReportMappingStore {
    return MemoryLegacyCarbonReportMappingStore(
        mutableMapOf<String, LegacyCarbonReportMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCarbonReportMappingStore(
    var inner: LegacyCarbonReportMappingStore,
    var cache: TtlCache<LegacyCarbonReportMapping>,
    var hits: Int,
    var misses: Int,
) : LegacyCarbonReportMappingStore {
    override fun find(code: String): LegacyCarbonReportMapping? {
        val hit = (cache.get(code) as LegacyCarbonReportMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCarbonReportMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCarbonReportMapping> = inner.list()
}

fun newCachedLegacyCarbonReportMappingStore(inner: LegacyCarbonReportMappingStore, ttl: Int, clock: Clock): LegacyCarbonReportMappingStore {
    return CachedLegacyCarbonReportMappingStore(
        inner = inner,
        cache = newTtlCache<LegacyCarbonReportMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy carbon report mappings. Empty fields match all. */
data class LegacyCarbonReportMappingQuery(
    var channel: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCarbonReportMapping): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyCarbonReportMapping>): MutableList<LegacyCarbonReportMapping> {
        val matched = filter(items) { candidate: LegacyCarbonReportMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCarbonReportMappings(store: LegacyCarbonReportMappingStore, query: LegacyCarbonReportMappingQuery): MutableList<LegacyCarbonReportMapping> {
    return query.apply(store.list())
}
