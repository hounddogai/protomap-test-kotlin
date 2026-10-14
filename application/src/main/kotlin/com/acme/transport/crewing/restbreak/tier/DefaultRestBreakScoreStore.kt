package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default rest break scores by code. */
interface DefaultRestBreakScoreStore {
    fun find(code: String): DefaultRestBreakScore?
    fun save(item: DefaultRestBreakScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRestBreakScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRestBreakScoreStore(
    var items: MutableMap<String, DefaultRestBreakScore>,
    var order: MutableList<String>,
) : DefaultRestBreakScoreStore {
    override fun find(code: String): DefaultRestBreakScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRestBreakScore) {
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

    override fun list(): MutableList<DefaultRestBreakScore> {
        val out = mutableListOf<DefaultRestBreakScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRestBreakScoreStore(): DefaultRestBreakScoreStore {
    return MemoryDefaultRestBreakScoreStore(
        mutableMapOf<String, DefaultRestBreakScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRestBreakScoreStore(
    var inner: DefaultRestBreakScoreStore,
    var cache: TtlCache<DefaultRestBreakScore>,
    var hits: Int,
    var misses: Int,
) : DefaultRestBreakScoreStore {
    override fun find(code: String): DefaultRestBreakScore? {
        val hit = (cache.get(code) as DefaultRestBreakScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRestBreakScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRestBreakScore> = inner.list()
}

fun newCachedDefaultRestBreakScoreStore(inner: DefaultRestBreakScoreStore, ttl: Int, clock: Clock): DefaultRestBreakScoreStore {
    return CachedDefaultRestBreakScoreStore(
        inner = inner,
        cache = newTtlCache<DefaultRestBreakScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default rest break scores. Empty fields match all. */
data class DefaultRestBreakScoreQuery(
    var segment: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRestBreakScore): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRestBreakScore>): MutableList<DefaultRestBreakScore> {
        val matched = filter(items) { candidate: DefaultRestBreakScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRestBreakScores(store: DefaultRestBreakScoreStore, query: DefaultRestBreakScoreQuery): MutableList<DefaultRestBreakScore> {
    return query.apply(store.list())
}
