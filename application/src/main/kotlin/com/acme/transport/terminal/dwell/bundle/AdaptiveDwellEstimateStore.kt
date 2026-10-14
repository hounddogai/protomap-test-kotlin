package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive dwell estimates by code. */
interface AdaptiveDwellEstimateStore {
    fun find(code: String): AdaptiveDwellEstimate?
    fun save(item: AdaptiveDwellEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDwellEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDwellEstimateStore(
    var items: MutableMap<String, AdaptiveDwellEstimate>,
    var order: MutableList<String>,
) : AdaptiveDwellEstimateStore {
    override fun find(code: String): AdaptiveDwellEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDwellEstimate) {
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

    override fun list(): MutableList<AdaptiveDwellEstimate> {
        val out = mutableListOf<AdaptiveDwellEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDwellEstimateStore(): AdaptiveDwellEstimateStore {
    return MemoryAdaptiveDwellEstimateStore(
        mutableMapOf<String, AdaptiveDwellEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDwellEstimateStore(
    var inner: AdaptiveDwellEstimateStore,
    var cache: TtlCache<AdaptiveDwellEstimate>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDwellEstimateStore {
    override fun find(code: String): AdaptiveDwellEstimate? {
        val hit = (cache.get(code) as AdaptiveDwellEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDwellEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDwellEstimate> = inner.list()
}

fun newCachedAdaptiveDwellEstimateStore(inner: AdaptiveDwellEstimateStore, ttl: Int, clock: Clock): AdaptiveDwellEstimateStore {
    return CachedAdaptiveDwellEstimateStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDwellEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive dwell estimates. Empty fields match all. */
data class AdaptiveDwellEstimateQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDwellEstimate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDwellEstimate>): MutableList<AdaptiveDwellEstimate> {
        val matched = filter(items) { candidate: AdaptiveDwellEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDwellEstimates(store: AdaptiveDwellEstimateStore, query: AdaptiveDwellEstimateQuery): MutableList<AdaptiveDwellEstimate> {
    return query.apply(store.list())
}
