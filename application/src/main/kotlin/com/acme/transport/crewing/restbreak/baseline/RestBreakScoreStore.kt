package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break scores by code. */
interface RestBreakScoreStore {
    fun find(code: String): RestBreakScore?
    fun save(item: RestBreakScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakScoreStore(
    var items: MutableMap<String, RestBreakScore>,
    var order: MutableList<String>,
) : RestBreakScoreStore {
    override fun find(code: String): RestBreakScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakScore) {
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

    override fun list(): MutableList<RestBreakScore> {
        val out = mutableListOf<RestBreakScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakScoreStore(): RestBreakScoreStore {
    return MemoryRestBreakScoreStore(mutableMapOf<String, RestBreakScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakScoreStore(
    var inner: RestBreakScoreStore,
    var cache: TtlCache<RestBreakScore>,
    var hits: Int,
    var misses: Int,
) : RestBreakScoreStore {
    override fun find(code: String): RestBreakScore? {
        val hit = (cache.get(code) as RestBreakScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakScore> = inner.list()
}

fun newCachedRestBreakScoreStore(inner: RestBreakScoreStore, ttl: Int, clock: Clock): RestBreakScoreStore {
    return CachedRestBreakScoreStore(
        inner = inner,
        cache = newTtlCache<RestBreakScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break scores. Empty fields match all. */
data class RestBreakScoreQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakScore>): MutableList<RestBreakScore> {
        val matched = filter(items) { candidate: RestBreakScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakScores(store: RestBreakScoreStore, query: RestBreakScoreQuery): MutableList<RestBreakScore> {
    return query.apply(store.list())
}
