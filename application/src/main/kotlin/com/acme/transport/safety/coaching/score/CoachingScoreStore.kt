package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching scores by code. */
interface CoachingScoreStore {
    fun find(code: String): CoachingScore?
    fun save(item: CoachingScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingScoreStore(
    var items: MutableMap<String, CoachingScore>,
    var order: MutableList<String>,
) : CoachingScoreStore {
    override fun find(code: String): CoachingScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingScore) {
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

    override fun list(): MutableList<CoachingScore> {
        val out = mutableListOf<CoachingScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingScoreStore(): CoachingScoreStore {
    return MemoryCoachingScoreStore(mutableMapOf<String, CoachingScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingScoreStore(
    var inner: CoachingScoreStore,
    var cache: TtlCache<CoachingScore>,
    var hits: Int,
    var misses: Int,
) : CoachingScoreStore {
    override fun find(code: String): CoachingScore? {
        val hit = (cache.get(code) as CoachingScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingScore> = inner.list()
}

fun newCachedCoachingScoreStore(inner: CoachingScoreStore, ttl: Int, clock: Clock): CoachingScoreStore {
    return CachedCoachingScoreStore(
        inner = inner,
        cache = newTtlCache<CoachingScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching scores. Empty fields match all. */
data class CoachingScoreQuery(
    var country: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingScore): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingScore>): MutableList<CoachingScore> {
        val matched = filter(items) { candidate: CoachingScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingScores(store: CoachingScoreStore, query: CoachingScoreQuery): MutableList<CoachingScore> {
    return query.apply(store.list())
}
