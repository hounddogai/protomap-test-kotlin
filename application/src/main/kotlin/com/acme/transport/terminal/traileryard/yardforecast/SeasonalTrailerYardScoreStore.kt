package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal trailer yard scores by code. */
interface SeasonalTrailerYardScoreStore {
    fun find(code: String): SeasonalTrailerYardScore?
    fun save(item: SeasonalTrailerYardScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTrailerYardScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTrailerYardScoreStore(
    var items: MutableMap<String, SeasonalTrailerYardScore>,
    var order: MutableList<String>,
) : SeasonalTrailerYardScoreStore {
    override fun find(code: String): SeasonalTrailerYardScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTrailerYardScore) {
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

    override fun list(): MutableList<SeasonalTrailerYardScore> {
        val out = mutableListOf<SeasonalTrailerYardScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTrailerYardScoreStore(): SeasonalTrailerYardScoreStore {
    return MemorySeasonalTrailerYardScoreStore(
        mutableMapOf<String, SeasonalTrailerYardScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTrailerYardScoreStore(
    var inner: SeasonalTrailerYardScoreStore,
    var cache: TtlCache<SeasonalTrailerYardScore>,
    var hits: Int,
    var misses: Int,
) : SeasonalTrailerYardScoreStore {
    override fun find(code: String): SeasonalTrailerYardScore? {
        val hit = (cache.get(code) as SeasonalTrailerYardScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTrailerYardScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTrailerYardScore> = inner.list()
}

fun newCachedSeasonalTrailerYardScoreStore(inner: SeasonalTrailerYardScoreStore, ttl: Int, clock: Clock): SeasonalTrailerYardScoreStore {
    return CachedSeasonalTrailerYardScoreStore(
        inner = inner,
        cache = newTtlCache<SeasonalTrailerYardScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal trailer yard scores. Empty fields match all. */
data class SeasonalTrailerYardScoreQuery(
    var category: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTrailerYardScore): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTrailerYardScore>): MutableList<SeasonalTrailerYardScore> {
        val matched = filter(items) { candidate: SeasonalTrailerYardScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTrailerYardScores(store: SeasonalTrailerYardScoreStore, query: SeasonalTrailerYardScoreQuery): MutableList<SeasonalTrailerYardScore> {
    return query.apply(store.list())
}
