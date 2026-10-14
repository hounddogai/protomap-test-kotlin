package com.acme.transport.emissions.idling.bundle

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict idling scores by code. */
interface StrictIdlingScoreStore {
    fun find(code: String): StrictIdlingScore?
    fun save(item: StrictIdlingScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictIdlingScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictIdlingScoreStore(
    var items: MutableMap<String, StrictIdlingScore>,
    var order: MutableList<String>,
) : StrictIdlingScoreStore {
    override fun find(code: String): StrictIdlingScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictIdlingScore) {
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

    override fun list(): MutableList<StrictIdlingScore> {
        val out = mutableListOf<StrictIdlingScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictIdlingScoreStore(): StrictIdlingScoreStore {
    return MemoryStrictIdlingScoreStore(mutableMapOf<String, StrictIdlingScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictIdlingScoreStore(
    var inner: StrictIdlingScoreStore,
    var cache: TtlCache<StrictIdlingScore>,
    var hits: Int,
    var misses: Int,
) : StrictIdlingScoreStore {
    override fun find(code: String): StrictIdlingScore? {
        val hit = (cache.get(code) as StrictIdlingScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictIdlingScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictIdlingScore> = inner.list()
}

fun newCachedStrictIdlingScoreStore(inner: StrictIdlingScoreStore, ttl: Int, clock: Clock): StrictIdlingScoreStore {
    return CachedStrictIdlingScoreStore(
        inner = inner,
        cache = newTtlCache<StrictIdlingScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict idling scores. Empty fields match all. */
data class StrictIdlingScoreQuery(
    var channel: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictIdlingScore): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictIdlingScore>): MutableList<StrictIdlingScore> {
        val matched = filter(items) { candidate: StrictIdlingScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictIdlingScores(store: StrictIdlingScoreStore, query: StrictIdlingScoreQuery): MutableList<StrictIdlingScore> {
    return query.apply(store.list())
}
