package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback accessorial scores by code. */
interface FallbackAccessorialScoreStore {
    fun find(code: String): FallbackAccessorialScore?
    fun save(item: FallbackAccessorialScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAccessorialScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAccessorialScoreStore(
    var items: MutableMap<String, FallbackAccessorialScore>,
    var order: MutableList<String>,
) : FallbackAccessorialScoreStore {
    override fun find(code: String): FallbackAccessorialScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAccessorialScore) {
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

    override fun list(): MutableList<FallbackAccessorialScore> {
        val out = mutableListOf<FallbackAccessorialScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAccessorialScoreStore(): FallbackAccessorialScoreStore {
    return MemoryFallbackAccessorialScoreStore(
        mutableMapOf<String, FallbackAccessorialScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAccessorialScoreStore(
    var inner: FallbackAccessorialScoreStore,
    var cache: TtlCache<FallbackAccessorialScore>,
    var hits: Int,
    var misses: Int,
) : FallbackAccessorialScoreStore {
    override fun find(code: String): FallbackAccessorialScore? {
        val hit = (cache.get(code) as FallbackAccessorialScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAccessorialScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAccessorialScore> = inner.list()
}

fun newCachedFallbackAccessorialScoreStore(inner: FallbackAccessorialScoreStore, ttl: Int, clock: Clock): FallbackAccessorialScoreStore {
    return CachedFallbackAccessorialScoreStore(
        inner = inner,
        cache = newTtlCache<FallbackAccessorialScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback accessorial scores. Empty fields match all. */
data class FallbackAccessorialScoreQuery(
    var depot: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAccessorialScore): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAccessorialScore>): MutableList<FallbackAccessorialScore> {
        val matched = filter(items) { candidate: FallbackAccessorialScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAccessorialScores(store: FallbackAccessorialScoreStore, query: FallbackAccessorialScoreQuery): MutableList<FallbackAccessorialScore> {
    return query.apply(store.list())
}
