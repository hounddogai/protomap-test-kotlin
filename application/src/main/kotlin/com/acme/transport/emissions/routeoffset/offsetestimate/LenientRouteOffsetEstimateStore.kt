package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient route offset estimates by code. */
interface LenientRouteOffsetEstimateStore {
    fun find(code: String): LenientRouteOffsetEstimate?
    fun save(item: LenientRouteOffsetEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRouteOffsetEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRouteOffsetEstimateStore(
    var items: MutableMap<String, LenientRouteOffsetEstimate>,
    var order: MutableList<String>,
) : LenientRouteOffsetEstimateStore {
    override fun find(code: String): LenientRouteOffsetEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRouteOffsetEstimate) {
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

    override fun list(): MutableList<LenientRouteOffsetEstimate> {
        val out = mutableListOf<LenientRouteOffsetEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRouteOffsetEstimateStore(): LenientRouteOffsetEstimateStore {
    return MemoryLenientRouteOffsetEstimateStore(
        mutableMapOf<String, LenientRouteOffsetEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRouteOffsetEstimateStore(
    var inner: LenientRouteOffsetEstimateStore,
    var cache: TtlCache<LenientRouteOffsetEstimate>,
    var hits: Int,
    var misses: Int,
) : LenientRouteOffsetEstimateStore {
    override fun find(code: String): LenientRouteOffsetEstimate? {
        val hit = (cache.get(code) as LenientRouteOffsetEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRouteOffsetEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRouteOffsetEstimate> = inner.list()
}

fun newCachedLenientRouteOffsetEstimateStore(inner: LenientRouteOffsetEstimateStore, ttl: Int, clock: Clock): LenientRouteOffsetEstimateStore {
    return CachedLenientRouteOffsetEstimateStore(
        inner = inner,
        cache = newTtlCache<LenientRouteOffsetEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient route offset estimates. Empty fields match all. */
data class LenientRouteOffsetEstimateQuery(
    var segment: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRouteOffsetEstimate): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRouteOffsetEstimate>): MutableList<LenientRouteOffsetEstimate> {
        val matched = filter(items) { candidate: LenientRouteOffsetEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRouteOffsetEstimates(store: LenientRouteOffsetEstimateStore, query: LenientRouteOffsetEstimateQuery): MutableList<LenientRouteOffsetEstimate> {
    return query.apply(store.list())
}
