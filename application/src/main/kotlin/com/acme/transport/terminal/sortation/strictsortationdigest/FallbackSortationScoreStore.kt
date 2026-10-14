package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback sortation scores by code. */
interface FallbackSortationScoreStore {
    fun find(code: String): FallbackSortationScore?
    fun save(item: FallbackSortationScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackSortationScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackSortationScoreStore(
    var items: MutableMap<String, FallbackSortationScore>,
    var order: MutableList<String>,
) : FallbackSortationScoreStore {
    override fun find(code: String): FallbackSortationScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackSortationScore) {
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

    override fun list(): MutableList<FallbackSortationScore> {
        val out = mutableListOf<FallbackSortationScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackSortationScoreStore(): FallbackSortationScoreStore {
    return MemoryFallbackSortationScoreStore(
        mutableMapOf<String, FallbackSortationScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackSortationScoreStore(
    var inner: FallbackSortationScoreStore,
    var cache: TtlCache<FallbackSortationScore>,
    var hits: Int,
    var misses: Int,
) : FallbackSortationScoreStore {
    override fun find(code: String): FallbackSortationScore? {
        val hit = (cache.get(code) as FallbackSortationScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackSortationScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackSortationScore> = inner.list()
}

fun newCachedFallbackSortationScoreStore(inner: FallbackSortationScoreStore, ttl: Int, clock: Clock): FallbackSortationScoreStore {
    return CachedFallbackSortationScoreStore(
        inner = inner,
        cache = newTtlCache<FallbackSortationScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback sortation scores. Empty fields match all. */
data class FallbackSortationScoreQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackSortationScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<FallbackSortationScore>): MutableList<FallbackSortationScore> {
        val matched = filter(items) { candidate: FallbackSortationScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackSortationScores(store: FallbackSortationScoreStore, query: FallbackSortationScoreQuery): MutableList<FallbackSortationScore> {
    return query.apply(store.list())
}
