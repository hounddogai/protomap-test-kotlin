package com.acme.transport.network.cutoff.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary cutoff scores by code. */
interface PrimaryCutoffScoreStore {
    fun find(code: String): PrimaryCutoffScore?
    fun save(item: PrimaryCutoffScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCutoffScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCutoffScoreStore(
    var items: MutableMap<String, PrimaryCutoffScore>,
    var order: MutableList<String>,
) : PrimaryCutoffScoreStore {
    override fun find(code: String): PrimaryCutoffScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCutoffScore) {
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

    override fun list(): MutableList<PrimaryCutoffScore> {
        val out = mutableListOf<PrimaryCutoffScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCutoffScoreStore(): PrimaryCutoffScoreStore {
    return MemoryPrimaryCutoffScoreStore(mutableMapOf<String, PrimaryCutoffScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCutoffScoreStore(
    var inner: PrimaryCutoffScoreStore,
    var cache: TtlCache<PrimaryCutoffScore>,
    var hits: Int,
    var misses: Int,
) : PrimaryCutoffScoreStore {
    override fun find(code: String): PrimaryCutoffScore? {
        val hit = (cache.get(code) as PrimaryCutoffScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCutoffScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCutoffScore> = inner.list()
}

fun newCachedPrimaryCutoffScoreStore(inner: PrimaryCutoffScoreStore, ttl: Int, clock: Clock): PrimaryCutoffScoreStore {
    return CachedPrimaryCutoffScoreStore(
        inner = inner,
        cache = newTtlCache<PrimaryCutoffScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary cutoff scores. Empty fields match all. */
data class PrimaryCutoffScoreQuery(
    var unit: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCutoffScore): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCutoffScore>): MutableList<PrimaryCutoffScore> {
        val matched = filter(items) { candidate: PrimaryCutoffScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCutoffScores(store: PrimaryCutoffScoreStore, query: PrimaryCutoffScoreQuery): MutableList<PrimaryCutoffScore> {
    return query.apply(store.list())
}
