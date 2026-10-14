package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal accessorial scores by code. */
interface SeasonalAccessorialScoreStore {
    fun find(code: String): SeasonalAccessorialScore?
    fun save(item: SeasonalAccessorialScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAccessorialScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAccessorialScoreStore(
    var items: MutableMap<String, SeasonalAccessorialScore>,
    var order: MutableList<String>,
) : SeasonalAccessorialScoreStore {
    override fun find(code: String): SeasonalAccessorialScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAccessorialScore) {
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

    override fun list(): MutableList<SeasonalAccessorialScore> {
        val out = mutableListOf<SeasonalAccessorialScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAccessorialScoreStore(): SeasonalAccessorialScoreStore {
    return MemorySeasonalAccessorialScoreStore(
        mutableMapOf<String, SeasonalAccessorialScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAccessorialScoreStore(
    var inner: SeasonalAccessorialScoreStore,
    var cache: TtlCache<SeasonalAccessorialScore>,
    var hits: Int,
    var misses: Int,
) : SeasonalAccessorialScoreStore {
    override fun find(code: String): SeasonalAccessorialScore? {
        val hit = (cache.get(code) as SeasonalAccessorialScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAccessorialScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAccessorialScore> = inner.list()
}

fun newCachedSeasonalAccessorialScoreStore(inner: SeasonalAccessorialScoreStore, ttl: Int, clock: Clock): SeasonalAccessorialScoreStore {
    return CachedSeasonalAccessorialScoreStore(
        inner = inner,
        cache = newTtlCache<SeasonalAccessorialScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal accessorial scores. Empty fields match all. */
data class SeasonalAccessorialScoreQuery(
    var locale: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAccessorialScore): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalAccessorialScore>): MutableList<SeasonalAccessorialScore> {
        val matched = filter(items) { candidate: SeasonalAccessorialScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAccessorialScores(store: SeasonalAccessorialScoreStore, query: SeasonalAccessorialScoreQuery): MutableList<SeasonalAccessorialScore> {
    return query.apply(store.list())
}
