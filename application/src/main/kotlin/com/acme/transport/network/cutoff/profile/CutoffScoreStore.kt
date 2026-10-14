package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff scores by code. */
interface CutoffScoreStore {
    fun find(code: String): CutoffScore?
    fun save(item: CutoffScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffScoreStore(
    var items: MutableMap<String, CutoffScore>,
    var order: MutableList<String>,
) : CutoffScoreStore {
    override fun find(code: String): CutoffScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffScore) {
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

    override fun list(): MutableList<CutoffScore> {
        val out = mutableListOf<CutoffScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffScoreStore(): CutoffScoreStore {
    return MemoryCutoffScoreStore(mutableMapOf<String, CutoffScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffScoreStore(
    var inner: CutoffScoreStore,
    var cache: TtlCache<CutoffScore>,
    var hits: Int,
    var misses: Int,
) : CutoffScoreStore {
    override fun find(code: String): CutoffScore? {
        val hit = (cache.get(code) as CutoffScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffScore> = inner.list()
}

fun newCachedCutoffScoreStore(inner: CutoffScoreStore, ttl: Int, clock: Clock): CutoffScoreStore {
    return CachedCutoffScoreStore(
        inner = inner,
        cache = newTtlCache<CutoffScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff scores. Empty fields match all. */
data class CutoffScoreQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<CutoffScore>): MutableList<CutoffScore> {
        val matched = filter(items) { candidate: CutoffScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffScores(store: CutoffScoreStore, query: CutoffScoreQuery): MutableList<CutoffScore> {
    return query.apply(store.list())
}
