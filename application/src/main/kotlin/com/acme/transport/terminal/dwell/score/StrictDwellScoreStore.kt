package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict dwell scores by code. */
interface StrictDwellScoreStore {
    fun find(code: String): StrictDwellScore?
    fun save(item: StrictDwellScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDwellScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDwellScoreStore(
    var items: MutableMap<String, StrictDwellScore>,
    var order: MutableList<String>,
) : StrictDwellScoreStore {
    override fun find(code: String): StrictDwellScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDwellScore) {
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

    override fun list(): MutableList<StrictDwellScore> {
        val out = mutableListOf<StrictDwellScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDwellScoreStore(): StrictDwellScoreStore {
    return MemoryStrictDwellScoreStore(mutableMapOf<String, StrictDwellScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDwellScoreStore(
    var inner: StrictDwellScoreStore,
    var cache: TtlCache<StrictDwellScore>,
    var hits: Int,
    var misses: Int,
) : StrictDwellScoreStore {
    override fun find(code: String): StrictDwellScore? {
        val hit = (cache.get(code) as StrictDwellScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDwellScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDwellScore> = inner.list()
}

fun newCachedStrictDwellScoreStore(inner: StrictDwellScoreStore, ttl: Int, clock: Clock): StrictDwellScoreStore {
    return CachedStrictDwellScoreStore(
        inner = inner,
        cache = newTtlCache<StrictDwellScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dwell scores. Empty fields match all. */
data class StrictDwellScoreQuery(
    var country: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDwellScore): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<StrictDwellScore>): MutableList<StrictDwellScore> {
        val matched = filter(items) { candidate: StrictDwellScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDwellScores(store: StrictDwellScoreStore, query: StrictDwellScoreQuery): MutableList<StrictDwellScore> {
    return query.apply(store.list())
}
