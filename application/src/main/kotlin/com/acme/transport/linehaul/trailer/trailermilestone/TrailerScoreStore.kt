package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer scores by code. */
interface TrailerScoreStore {
    fun find(code: String): TrailerScore?
    fun save(item: TrailerScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerScoreStore(
    var items: MutableMap<String, TrailerScore>,
    var order: MutableList<String>,
) : TrailerScoreStore {
    override fun find(code: String): TrailerScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerScore) {
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

    override fun list(): MutableList<TrailerScore> {
        val out = mutableListOf<TrailerScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerScoreStore(): TrailerScoreStore {
    return MemoryTrailerScoreStore(mutableMapOf<String, TrailerScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerScoreStore(
    var inner: TrailerScoreStore,
    var cache: TtlCache<TrailerScore>,
    var hits: Int,
    var misses: Int,
) : TrailerScoreStore {
    override fun find(code: String): TrailerScore? {
        val hit = (cache.get(code) as TrailerScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerScore> = inner.list()
}

fun newCachedTrailerScoreStore(inner: TrailerScoreStore, ttl: Int, clock: Clock): TrailerScoreStore {
    return CachedTrailerScoreStore(
        inner = inner,
        cache = newTtlCache<TrailerScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer scores. Empty fields match all. */
data class TrailerScoreQuery(
    var unit: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerScore): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerScore>): MutableList<TrailerScore> {
        val matched = filter(items) { candidate: TrailerScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerScores(store: TrailerScoreStore, query: TrailerScoreQuery): MutableList<TrailerScore> {
    return query.apply(store.list())
}
