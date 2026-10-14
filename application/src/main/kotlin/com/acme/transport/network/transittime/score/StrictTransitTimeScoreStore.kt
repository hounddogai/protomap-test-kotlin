package com.acme.transport.network.transittime.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict transit time scores by code. */
interface StrictTransitTimeScoreStore {
    fun find(code: String): StrictTransitTimeScore?
    fun save(item: StrictTransitTimeScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTransitTimeScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTransitTimeScoreStore(
    var items: MutableMap<String, StrictTransitTimeScore>,
    var order: MutableList<String>,
) : StrictTransitTimeScoreStore {
    override fun find(code: String): StrictTransitTimeScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTransitTimeScore) {
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

    override fun list(): MutableList<StrictTransitTimeScore> {
        val out = mutableListOf<StrictTransitTimeScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTransitTimeScoreStore(): StrictTransitTimeScoreStore {
    return MemoryStrictTransitTimeScoreStore(
        mutableMapOf<String, StrictTransitTimeScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTransitTimeScoreStore(
    var inner: StrictTransitTimeScoreStore,
    var cache: TtlCache<StrictTransitTimeScore>,
    var hits: Int,
    var misses: Int,
) : StrictTransitTimeScoreStore {
    override fun find(code: String): StrictTransitTimeScore? {
        val hit = (cache.get(code) as StrictTransitTimeScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTransitTimeScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTransitTimeScore> = inner.list()
}

fun newCachedStrictTransitTimeScoreStore(inner: StrictTransitTimeScoreStore, ttl: Int, clock: Clock): StrictTransitTimeScoreStore {
    return CachedStrictTransitTimeScoreStore(
        inner = inner,
        cache = newTtlCache<StrictTransitTimeScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict transit time scores. Empty fields match all. */
data class StrictTransitTimeScoreQuery(
    var depot: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTransitTimeScore): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTransitTimeScore>): MutableList<StrictTransitTimeScore> {
        val matched = filter(items) { candidate: StrictTransitTimeScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTransitTimeScores(store: StrictTransitTimeScoreStore, query: StrictTransitTimeScoreQuery): MutableList<StrictTransitTimeScore> {
    return query.apply(store.list())
}
