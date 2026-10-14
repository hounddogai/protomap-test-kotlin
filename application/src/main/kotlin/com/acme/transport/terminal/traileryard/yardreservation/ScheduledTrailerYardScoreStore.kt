package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled trailer yard scores by code. */
interface ScheduledTrailerYardScoreStore {
    fun find(code: String): ScheduledTrailerYardScore?
    fun save(item: ScheduledTrailerYardScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTrailerYardScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTrailerYardScoreStore(
    var items: MutableMap<String, ScheduledTrailerYardScore>,
    var order: MutableList<String>,
) : ScheduledTrailerYardScoreStore {
    override fun find(code: String): ScheduledTrailerYardScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTrailerYardScore) {
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

    override fun list(): MutableList<ScheduledTrailerYardScore> {
        val out = mutableListOf<ScheduledTrailerYardScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTrailerYardScoreStore(): ScheduledTrailerYardScoreStore {
    return MemoryScheduledTrailerYardScoreStore(
        mutableMapOf<String, ScheduledTrailerYardScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTrailerYardScoreStore(
    var inner: ScheduledTrailerYardScoreStore,
    var cache: TtlCache<ScheduledTrailerYardScore>,
    var hits: Int,
    var misses: Int,
) : ScheduledTrailerYardScoreStore {
    override fun find(code: String): ScheduledTrailerYardScore? {
        val hit = (cache.get(code) as ScheduledTrailerYardScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTrailerYardScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTrailerYardScore> = inner.list()
}

fun newCachedScheduledTrailerYardScoreStore(inner: ScheduledTrailerYardScoreStore, ttl: Int, clock: Clock): ScheduledTrailerYardScoreStore {
    return CachedScheduledTrailerYardScoreStore(
        inner = inner,
        cache = newTtlCache<ScheduledTrailerYardScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled trailer yard scores. Empty fields match all. */
data class ScheduledTrailerYardScoreQuery(
    var country: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTrailerYardScore): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTrailerYardScore>): MutableList<ScheduledTrailerYardScore> {
        val matched = filter(items) { candidate: ScheduledTrailerYardScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTrailerYardScores(store: ScheduledTrailerYardScoreStore, query: ScheduledTrailerYardScoreQuery): MutableList<ScheduledTrailerYardScore> {
    return query.apply(store.list())
}
