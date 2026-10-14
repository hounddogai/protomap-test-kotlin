package com.acme.transport.emissions.carbonreport.reportthreshold

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive carbon report thresholds by code. */
interface AdaptiveCarbonReportThresholdStore {
    fun find(code: String): AdaptiveCarbonReportThreshold?
    fun save(item: AdaptiveCarbonReportThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCarbonReportThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCarbonReportThresholdStore(
    var items: MutableMap<String, AdaptiveCarbonReportThreshold>,
    var order: MutableList<String>,
) : AdaptiveCarbonReportThresholdStore {
    override fun find(code: String): AdaptiveCarbonReportThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCarbonReportThreshold) {
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

    override fun list(): MutableList<AdaptiveCarbonReportThreshold> {
        val out = mutableListOf<AdaptiveCarbonReportThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCarbonReportThresholdStore(): AdaptiveCarbonReportThresholdStore {
    return MemoryAdaptiveCarbonReportThresholdStore(
        mutableMapOf<String, AdaptiveCarbonReportThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCarbonReportThresholdStore(
    var inner: AdaptiveCarbonReportThresholdStore,
    var cache: TtlCache<AdaptiveCarbonReportThreshold>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCarbonReportThresholdStore {
    override fun find(code: String): AdaptiveCarbonReportThreshold? {
        val hit = (cache.get(code) as AdaptiveCarbonReportThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCarbonReportThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCarbonReportThreshold> = inner.list()
}

fun newCachedAdaptiveCarbonReportThresholdStore(inner: AdaptiveCarbonReportThresholdStore, ttl: Int, clock: Clock): AdaptiveCarbonReportThresholdStore {
    return CachedAdaptiveCarbonReportThresholdStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCarbonReportThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive carbon report thresholds. Empty fields match all. */
data class AdaptiveCarbonReportThresholdQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCarbonReportThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveCarbonReportThreshold>): MutableList<AdaptiveCarbonReportThreshold> {
        val matched = filter(items) { candidate: AdaptiveCarbonReportThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCarbonReportThresholds(store: AdaptiveCarbonReportThresholdStore, query: AdaptiveCarbonReportThresholdQuery): MutableList<AdaptiveCarbonReportThreshold> {
    return query.apply(store.list())
}
