package com.acme.transport.crewing.swap.score

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap scores by code. */
interface SwapScoreStore {
    fun find(code: String): SwapScore?
    fun save(item: SwapScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapScoreStore(
    var items: MutableMap<String, SwapScore>,
    var order: MutableList<String>,
) : SwapScoreStore {
    override fun find(code: String): SwapScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapScore) {
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

    override fun list(): MutableList<SwapScore> {
        val out = mutableListOf<SwapScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapScoreStore(): SwapScoreStore {
    return MemorySwapScoreStore(mutableMapOf<String, SwapScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapScoreStore(
    var inner: SwapScoreStore,
    var cache: TtlCache<SwapScore>,
    var hits: Int,
    var misses: Int,
) : SwapScoreStore {
    override fun find(code: String): SwapScore? {
        val hit = (cache.get(code) as SwapScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapScore> = inner.list()
}

fun newCachedSwapScoreStore(inner: SwapScoreStore, ttl: Int, clock: Clock): SwapScoreStore {
    return CachedSwapScoreStore(
        inner = inner,
        cache = newTtlCache<SwapScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap scores. Empty fields match all. */
data class SwapScoreQuery(
    var segment: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapScore): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapScore>): MutableList<SwapScore> {
        val matched = filter(items) { candidate: SwapScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapScores(store: SwapScoreStore, query: SwapScoreQuery): MutableList<SwapScore> {
    return query.apply(store.list())
}
