package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive swap scores by code. */
interface AdaptiveSwapScoreStore {
    fun find(code: String): AdaptiveSwapScore?
    fun save(item: AdaptiveSwapScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveSwapScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveSwapScoreStore(
    var items: MutableMap<String, AdaptiveSwapScore>,
    var order: MutableList<String>,
) : AdaptiveSwapScoreStore {
    override fun find(code: String): AdaptiveSwapScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveSwapScore) {
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

    override fun list(): MutableList<AdaptiveSwapScore> {
        val out = mutableListOf<AdaptiveSwapScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveSwapScoreStore(): AdaptiveSwapScoreStore {
    return MemoryAdaptiveSwapScoreStore(mutableMapOf<String, AdaptiveSwapScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveSwapScoreStore(
    var inner: AdaptiveSwapScoreStore,
    var cache: TtlCache<AdaptiveSwapScore>,
    var hits: Int,
    var misses: Int,
) : AdaptiveSwapScoreStore {
    override fun find(code: String): AdaptiveSwapScore? {
        val hit = (cache.get(code) as AdaptiveSwapScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveSwapScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveSwapScore> = inner.list()
}

fun newCachedAdaptiveSwapScoreStore(inner: AdaptiveSwapScoreStore, ttl: Int, clock: Clock): AdaptiveSwapScoreStore {
    return CachedAdaptiveSwapScoreStore(
        inner = inner,
        cache = newTtlCache<AdaptiveSwapScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive swap scores. Empty fields match all. */
data class AdaptiveSwapScoreQuery(
    var segment: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveSwapScore): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveSwapScore>): MutableList<AdaptiveSwapScore> {
        val matched = filter(items) { candidate: AdaptiveSwapScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveSwapScores(store: AdaptiveSwapScoreStore, query: AdaptiveSwapScoreQuery): MutableList<AdaptiveSwapScore> {
    return query.apply(store.list())
}
