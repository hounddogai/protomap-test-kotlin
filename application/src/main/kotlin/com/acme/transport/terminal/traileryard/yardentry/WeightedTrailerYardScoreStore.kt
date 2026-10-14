package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted trailer yard scores by code. */
interface WeightedTrailerYardScoreStore {
    fun find(code: String): WeightedTrailerYardScore?
    fun save(item: WeightedTrailerYardScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTrailerYardScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTrailerYardScoreStore(
    var items: MutableMap<String, WeightedTrailerYardScore>,
    var order: MutableList<String>,
) : WeightedTrailerYardScoreStore {
    override fun find(code: String): WeightedTrailerYardScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTrailerYardScore) {
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

    override fun list(): MutableList<WeightedTrailerYardScore> {
        val out = mutableListOf<WeightedTrailerYardScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTrailerYardScoreStore(): WeightedTrailerYardScoreStore {
    return MemoryWeightedTrailerYardScoreStore(
        mutableMapOf<String, WeightedTrailerYardScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTrailerYardScoreStore(
    var inner: WeightedTrailerYardScoreStore,
    var cache: TtlCache<WeightedTrailerYardScore>,
    var hits: Int,
    var misses: Int,
) : WeightedTrailerYardScoreStore {
    override fun find(code: String): WeightedTrailerYardScore? {
        val hit = (cache.get(code) as WeightedTrailerYardScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTrailerYardScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTrailerYardScore> = inner.list()
}

fun newCachedWeightedTrailerYardScoreStore(inner: WeightedTrailerYardScoreStore, ttl: Int, clock: Clock): WeightedTrailerYardScoreStore {
    return CachedWeightedTrailerYardScoreStore(
        inner = inner,
        cache = newTtlCache<WeightedTrailerYardScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted trailer yard scores. Empty fields match all. */
data class WeightedTrailerYardScoreQuery(
    var source: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTrailerYardScore): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTrailerYardScore>): MutableList<WeightedTrailerYardScore> {
        val matched = filter(items) { candidate: WeightedTrailerYardScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTrailerYardScores(store: WeightedTrailerYardScoreStore, query: WeightedTrailerYardScoreQuery): MutableList<WeightedTrailerYardScore> {
    return query.apply(store.list())
}
