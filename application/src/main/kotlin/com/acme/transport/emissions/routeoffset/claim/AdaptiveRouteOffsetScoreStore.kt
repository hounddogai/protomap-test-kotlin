package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive route offset scores by code. */
interface AdaptiveRouteOffsetScoreStore {
    fun find(code: String): AdaptiveRouteOffsetScore?
    fun save(item: AdaptiveRouteOffsetScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveRouteOffsetScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveRouteOffsetScoreStore(
    var items: MutableMap<String, AdaptiveRouteOffsetScore>,
    var order: MutableList<String>,
) : AdaptiveRouteOffsetScoreStore {
    override fun find(code: String): AdaptiveRouteOffsetScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveRouteOffsetScore) {
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

    override fun list(): MutableList<AdaptiveRouteOffsetScore> {
        val out = mutableListOf<AdaptiveRouteOffsetScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveRouteOffsetScoreStore(): AdaptiveRouteOffsetScoreStore {
    return MemoryAdaptiveRouteOffsetScoreStore(
        mutableMapOf<String, AdaptiveRouteOffsetScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveRouteOffsetScoreStore(
    var inner: AdaptiveRouteOffsetScoreStore,
    var cache: TtlCache<AdaptiveRouteOffsetScore>,
    var hits: Int,
    var misses: Int,
) : AdaptiveRouteOffsetScoreStore {
    override fun find(code: String): AdaptiveRouteOffsetScore? {
        val hit = (cache.get(code) as AdaptiveRouteOffsetScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveRouteOffsetScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveRouteOffsetScore> = inner.list()
}

fun newCachedAdaptiveRouteOffsetScoreStore(inner: AdaptiveRouteOffsetScoreStore, ttl: Int, clock: Clock): AdaptiveRouteOffsetScoreStore {
    return CachedAdaptiveRouteOffsetScoreStore(
        inner = inner,
        cache = newTtlCache<AdaptiveRouteOffsetScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive route offset scores. Empty fields match all. */
data class AdaptiveRouteOffsetScoreQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveRouteOffsetScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveRouteOffsetScore>): MutableList<AdaptiveRouteOffsetScore> {
        val matched = filter(items) { candidate: AdaptiveRouteOffsetScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveRouteOffsetScores(store: AdaptiveRouteOffsetScoreStore, query: AdaptiveRouteOffsetScoreQuery): MutableList<AdaptiveRouteOffsetScore> {
    return query.apply(store.list())
}
