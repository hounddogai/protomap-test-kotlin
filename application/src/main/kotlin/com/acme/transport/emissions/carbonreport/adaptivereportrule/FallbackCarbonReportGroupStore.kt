package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback carbon report groups by code. */
interface FallbackCarbonReportGroupStore {
    fun find(code: String): FallbackCarbonReportGroup?
    fun save(item: FallbackCarbonReportGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackCarbonReportGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackCarbonReportGroupStore(
    var items: MutableMap<String, FallbackCarbonReportGroup>,
    var order: MutableList<String>,
) : FallbackCarbonReportGroupStore {
    override fun find(code: String): FallbackCarbonReportGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackCarbonReportGroup) {
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

    override fun list(): MutableList<FallbackCarbonReportGroup> {
        val out = mutableListOf<FallbackCarbonReportGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackCarbonReportGroupStore(): FallbackCarbonReportGroupStore {
    return MemoryFallbackCarbonReportGroupStore(
        mutableMapOf<String, FallbackCarbonReportGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackCarbonReportGroupStore(
    var inner: FallbackCarbonReportGroupStore,
    var cache: TtlCache<FallbackCarbonReportGroup>,
    var hits: Int,
    var misses: Int,
) : FallbackCarbonReportGroupStore {
    override fun find(code: String): FallbackCarbonReportGroup? {
        val hit = (cache.get(code) as FallbackCarbonReportGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackCarbonReportGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackCarbonReportGroup> = inner.list()
}

fun newCachedFallbackCarbonReportGroupStore(inner: FallbackCarbonReportGroupStore, ttl: Int, clock: Clock): FallbackCarbonReportGroupStore {
    return CachedFallbackCarbonReportGroupStore(
        inner = inner,
        cache = newTtlCache<FallbackCarbonReportGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback carbon report groups. Empty fields match all. */
data class FallbackCarbonReportGroupQuery(
    var category: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackCarbonReportGroup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<FallbackCarbonReportGroup>): MutableList<FallbackCarbonReportGroup> {
        val matched = filter(items) { candidate: FallbackCarbonReportGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackCarbonReportGroups(store: FallbackCarbonReportGroupStore, query: FallbackCarbonReportGroupQuery): MutableList<FallbackCarbonReportGroup> {
    return query.apply(store.list())
}
