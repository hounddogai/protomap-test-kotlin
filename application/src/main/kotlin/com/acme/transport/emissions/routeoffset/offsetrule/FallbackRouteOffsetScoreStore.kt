package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback route offset scores by code. */
interface FallbackRouteOffsetScoreStore {
    fun find(code: String): FallbackRouteOffsetScore?
    fun save(item: FallbackRouteOffsetScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRouteOffsetScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRouteOffsetScoreStore(
    var items: MutableMap<String, FallbackRouteOffsetScore>,
    var order: MutableList<String>,
) : FallbackRouteOffsetScoreStore {
    override fun find(code: String): FallbackRouteOffsetScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRouteOffsetScore) {
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

    override fun list(): MutableList<FallbackRouteOffsetScore> {
        val out = mutableListOf<FallbackRouteOffsetScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRouteOffsetScoreStore(): FallbackRouteOffsetScoreStore {
    return MemoryFallbackRouteOffsetScoreStore(
        mutableMapOf<String, FallbackRouteOffsetScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRouteOffsetScoreStore(
    var inner: FallbackRouteOffsetScoreStore,
    var cache: TtlCache<FallbackRouteOffsetScore>,
    var hits: Int,
    var misses: Int,
) : FallbackRouteOffsetScoreStore {
    override fun find(code: String): FallbackRouteOffsetScore? {
        val hit = (cache.get(code) as FallbackRouteOffsetScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRouteOffsetScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRouteOffsetScore> = inner.list()
}

fun newCachedFallbackRouteOffsetScoreStore(inner: FallbackRouteOffsetScoreStore, ttl: Int, clock: Clock): FallbackRouteOffsetScoreStore {
    return CachedFallbackRouteOffsetScoreStore(
        inner = inner,
        cache = newTtlCache<FallbackRouteOffsetScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback route offset scores. Empty fields match all. */
data class FallbackRouteOffsetScoreQuery(
    var lane: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRouteOffsetScore): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<FallbackRouteOffsetScore>): MutableList<FallbackRouteOffsetScore> {
        val matched = filter(items) { candidate: FallbackRouteOffsetScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRouteOffsetScores(store: FallbackRouteOffsetScoreStore, query: FallbackRouteOffsetScoreQuery): MutableList<FallbackRouteOffsetScore> {
    return query.apply(store.list())
}
