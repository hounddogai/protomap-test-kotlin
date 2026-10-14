package com.acme.transport.customs.inspection.target

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive inspection estimates by code. */
interface AdaptiveInspectionEstimateStore {
    fun find(code: String): AdaptiveInspectionEstimate?
    fun save(item: AdaptiveInspectionEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveInspectionEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveInspectionEstimateStore(
    var items: MutableMap<String, AdaptiveInspectionEstimate>,
    var order: MutableList<String>,
) : AdaptiveInspectionEstimateStore {
    override fun find(code: String): AdaptiveInspectionEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveInspectionEstimate) {
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

    override fun list(): MutableList<AdaptiveInspectionEstimate> {
        val out = mutableListOf<AdaptiveInspectionEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveInspectionEstimateStore(): AdaptiveInspectionEstimateStore {
    return MemoryAdaptiveInspectionEstimateStore(
        mutableMapOf<String, AdaptiveInspectionEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveInspectionEstimateStore(
    var inner: AdaptiveInspectionEstimateStore,
    var cache: TtlCache<AdaptiveInspectionEstimate>,
    var hits: Int,
    var misses: Int,
) : AdaptiveInspectionEstimateStore {
    override fun find(code: String): AdaptiveInspectionEstimate? {
        val hit = (cache.get(code) as AdaptiveInspectionEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveInspectionEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveInspectionEstimate> = inner.list()
}

fun newCachedAdaptiveInspectionEstimateStore(inner: AdaptiveInspectionEstimateStore, ttl: Int, clock: Clock): AdaptiveInspectionEstimateStore {
    return CachedAdaptiveInspectionEstimateStore(
        inner = inner,
        cache = newTtlCache<AdaptiveInspectionEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive inspection estimates. Empty fields match all. */
data class AdaptiveInspectionEstimateQuery(
    var source: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveInspectionEstimate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveInspectionEstimate>): MutableList<AdaptiveInspectionEstimate> {
        val matched = filter(items) { candidate: AdaptiveInspectionEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveInspectionEstimates(store: AdaptiveInspectionEstimateStore, query: AdaptiveInspectionEstimateQuery): MutableList<AdaptiveInspectionEstimate> {
    return query.apply(store.list())
}
