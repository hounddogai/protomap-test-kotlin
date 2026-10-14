package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal dock door scores by code. */
interface SeasonalDockDoorScoreStore {
    fun find(code: String): SeasonalDockDoorScore?
    fun save(item: SeasonalDockDoorScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDockDoorScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDockDoorScoreStore(
    var items: MutableMap<String, SeasonalDockDoorScore>,
    var order: MutableList<String>,
) : SeasonalDockDoorScoreStore {
    override fun find(code: String): SeasonalDockDoorScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDockDoorScore) {
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

    override fun list(): MutableList<SeasonalDockDoorScore> {
        val out = mutableListOf<SeasonalDockDoorScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDockDoorScoreStore(): SeasonalDockDoorScoreStore {
    return MemorySeasonalDockDoorScoreStore(
        mutableMapOf<String, SeasonalDockDoorScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDockDoorScoreStore(
    var inner: SeasonalDockDoorScoreStore,
    var cache: TtlCache<SeasonalDockDoorScore>,
    var hits: Int,
    var misses: Int,
) : SeasonalDockDoorScoreStore {
    override fun find(code: String): SeasonalDockDoorScore? {
        val hit = (cache.get(code) as SeasonalDockDoorScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDockDoorScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDockDoorScore> = inner.list()
}

fun newCachedSeasonalDockDoorScoreStore(inner: SeasonalDockDoorScoreStore, ttl: Int, clock: Clock): SeasonalDockDoorScoreStore {
    return CachedSeasonalDockDoorScoreStore(
        inner = inner,
        cache = newTtlCache<SeasonalDockDoorScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal dock door scores. Empty fields match all. */
data class SeasonalDockDoorScoreQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDockDoorScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<SeasonalDockDoorScore>): MutableList<SeasonalDockDoorScore> {
        val matched = filter(items) { candidate: SeasonalDockDoorScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDockDoorScores(store: SeasonalDockDoorScoreStore, query: SeasonalDockDoorScoreQuery): MutableList<SeasonalDockDoorScore> {
    return query.apply(store.list())
}
