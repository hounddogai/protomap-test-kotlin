package com.acme.transport.terminal.dwell.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback dwell scores by code. */
interface FallbackDwellScoreStore {
    fun find(code: String): FallbackDwellScore?
    fun save(item: FallbackDwellScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDwellScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDwellScoreStore(
    var items: MutableMap<String, FallbackDwellScore>,
    var order: MutableList<String>,
) : FallbackDwellScoreStore {
    override fun find(code: String): FallbackDwellScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDwellScore) {
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

    override fun list(): MutableList<FallbackDwellScore> {
        val out = mutableListOf<FallbackDwellScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDwellScoreStore(): FallbackDwellScoreStore {
    return MemoryFallbackDwellScoreStore(mutableMapOf<String, FallbackDwellScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDwellScoreStore(
    var inner: FallbackDwellScoreStore,
    var cache: TtlCache<FallbackDwellScore>,
    var hits: Int,
    var misses: Int,
) : FallbackDwellScoreStore {
    override fun find(code: String): FallbackDwellScore? {
        val hit = (cache.get(code) as FallbackDwellScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDwellScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDwellScore> = inner.list()
}

fun newCachedFallbackDwellScoreStore(inner: FallbackDwellScoreStore, ttl: Int, clock: Clock): FallbackDwellScoreStore {
    return CachedFallbackDwellScoreStore(
        inner = inner,
        cache = newTtlCache<FallbackDwellScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dwell scores. Empty fields match all. */
data class FallbackDwellScoreQuery(
    var depot: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDwellScore): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDwellScore>): MutableList<FallbackDwellScore> {
        val matched = filter(items) { candidate: FallbackDwellScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDwellScores(store: FallbackDwellScoreStore, query: FallbackDwellScoreQuery): MutableList<FallbackDwellScore> {
    return query.apply(store.list())
}
