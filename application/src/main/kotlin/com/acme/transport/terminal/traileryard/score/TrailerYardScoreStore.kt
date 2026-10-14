package com.acme.transport.terminal.traileryard.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard scores by code. */
interface TrailerYardScoreStore {
    fun find(code: String): TrailerYardScore?
    fun save(item: TrailerYardScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardScoreStore(
    var items: MutableMap<String, TrailerYardScore>,
    var order: MutableList<String>,
) : TrailerYardScoreStore {
    override fun find(code: String): TrailerYardScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardScore) {
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

    override fun list(): MutableList<TrailerYardScore> {
        val out = mutableListOf<TrailerYardScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardScoreStore(): TrailerYardScoreStore {
    return MemoryTrailerYardScoreStore(mutableMapOf<String, TrailerYardScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardScoreStore(
    var inner: TrailerYardScoreStore,
    var cache: TtlCache<TrailerYardScore>,
    var hits: Int,
    var misses: Int,
) : TrailerYardScoreStore {
    override fun find(code: String): TrailerYardScore? {
        val hit = (cache.get(code) as TrailerYardScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardScore> = inner.list()
}

fun newCachedTrailerYardScoreStore(inner: TrailerYardScoreStore, ttl: Int, clock: Clock): TrailerYardScoreStore {
    return CachedTrailerYardScoreStore(
        inner = inner,
        cache = newTtlCache<TrailerYardScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard scores. Empty fields match all. */
data class TrailerYardScoreQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TrailerYardScore>): MutableList<TrailerYardScore> {
        val matched = filter(items) { candidate: TrailerYardScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardScores(store: TrailerYardScoreStore, query: TrailerYardScoreQuery): MutableList<TrailerYardScore> {
    return query.apply(store.list())
}
