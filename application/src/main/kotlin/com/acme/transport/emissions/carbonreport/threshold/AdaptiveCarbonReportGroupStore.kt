package com.acme.transport.emissions.carbonreport.threshold

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive carbon report groups by code. */
interface AdaptiveCarbonReportGroupStore {
    fun find(code: String): AdaptiveCarbonReportGroup?
    fun save(item: AdaptiveCarbonReportGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCarbonReportGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCarbonReportGroupStore(
    var items: MutableMap<String, AdaptiveCarbonReportGroup>,
    var order: MutableList<String>,
) : AdaptiveCarbonReportGroupStore {
    override fun find(code: String): AdaptiveCarbonReportGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCarbonReportGroup) {
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

    override fun list(): MutableList<AdaptiveCarbonReportGroup> {
        val out = mutableListOf<AdaptiveCarbonReportGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCarbonReportGroupStore(): AdaptiveCarbonReportGroupStore {
    return MemoryAdaptiveCarbonReportGroupStore(
        mutableMapOf<String, AdaptiveCarbonReportGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCarbonReportGroupStore(
    var inner: AdaptiveCarbonReportGroupStore,
    var cache: TtlCache<AdaptiveCarbonReportGroup>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCarbonReportGroupStore {
    override fun find(code: String): AdaptiveCarbonReportGroup? {
        val hit = (cache.get(code) as AdaptiveCarbonReportGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCarbonReportGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCarbonReportGroup> = inner.list()
}

fun newCachedAdaptiveCarbonReportGroupStore(inner: AdaptiveCarbonReportGroupStore, ttl: Int, clock: Clock): AdaptiveCarbonReportGroupStore {
    return CachedAdaptiveCarbonReportGroupStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCarbonReportGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive carbon report groups. Empty fields match all. */
data class AdaptiveCarbonReportGroupQuery(
    var country: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCarbonReportGroup): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveCarbonReportGroup>): MutableList<AdaptiveCarbonReportGroup> {
        val matched = filter(items) { candidate: AdaptiveCarbonReportGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCarbonReportGroups(store: AdaptiveCarbonReportGroupStore, query: AdaptiveCarbonReportGroupQuery): MutableList<AdaptiveCarbonReportGroup> {
    return query.apply(store.list())
}
