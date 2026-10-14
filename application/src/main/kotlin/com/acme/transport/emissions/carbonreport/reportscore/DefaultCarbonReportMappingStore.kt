package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default carbon report mappings by code. */
interface DefaultCarbonReportMappingStore {
    fun find(code: String): DefaultCarbonReportMapping?
    fun save(item: DefaultCarbonReportMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCarbonReportMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCarbonReportMappingStore(
    var items: MutableMap<String, DefaultCarbonReportMapping>,
    var order: MutableList<String>,
) : DefaultCarbonReportMappingStore {
    override fun find(code: String): DefaultCarbonReportMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCarbonReportMapping) {
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

    override fun list(): MutableList<DefaultCarbonReportMapping> {
        val out = mutableListOf<DefaultCarbonReportMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCarbonReportMappingStore(): DefaultCarbonReportMappingStore {
    return MemoryDefaultCarbonReportMappingStore(
        mutableMapOf<String, DefaultCarbonReportMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCarbonReportMappingStore(
    var inner: DefaultCarbonReportMappingStore,
    var cache: TtlCache<DefaultCarbonReportMapping>,
    var hits: Int,
    var misses: Int,
) : DefaultCarbonReportMappingStore {
    override fun find(code: String): DefaultCarbonReportMapping? {
        val hit = (cache.get(code) as DefaultCarbonReportMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCarbonReportMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCarbonReportMapping> = inner.list()
}

fun newCachedDefaultCarbonReportMappingStore(inner: DefaultCarbonReportMappingStore, ttl: Int, clock: Clock): DefaultCarbonReportMappingStore {
    return CachedDefaultCarbonReportMappingStore(
        inner = inner,
        cache = newTtlCache<DefaultCarbonReportMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default carbon report mappings. Empty fields match all. */
data class DefaultCarbonReportMappingQuery(
    var segment: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCarbonReportMapping): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCarbonReportMapping>): MutableList<DefaultCarbonReportMapping> {
        val matched = filter(items) { candidate: DefaultCarbonReportMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCarbonReportMappings(store: DefaultCarbonReportMappingStore, query: DefaultCarbonReportMappingQuery): MutableList<DefaultCarbonReportMapping> {
    return query.apply(store.list())
}
