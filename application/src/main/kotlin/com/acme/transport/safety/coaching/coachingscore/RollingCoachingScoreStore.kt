package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling coaching scores by code. */
interface RollingCoachingScoreStore {
    fun find(code: String): RollingCoachingScore?
    fun save(item: RollingCoachingScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCoachingScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCoachingScoreStore(
    var items: MutableMap<String, RollingCoachingScore>,
    var order: MutableList<String>,
) : RollingCoachingScoreStore {
    override fun find(code: String): RollingCoachingScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCoachingScore) {
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

    override fun list(): MutableList<RollingCoachingScore> {
        val out = mutableListOf<RollingCoachingScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCoachingScoreStore(): RollingCoachingScoreStore {
    return MemoryRollingCoachingScoreStore(
        mutableMapOf<String, RollingCoachingScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCoachingScoreStore(
    var inner: RollingCoachingScoreStore,
    var cache: TtlCache<RollingCoachingScore>,
    var hits: Int,
    var misses: Int,
) : RollingCoachingScoreStore {
    override fun find(code: String): RollingCoachingScore? {
        val hit = (cache.get(code) as RollingCoachingScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCoachingScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCoachingScore> = inner.list()
}

fun newCachedRollingCoachingScoreStore(inner: RollingCoachingScoreStore, ttl: Int, clock: Clock): RollingCoachingScoreStore {
    return CachedRollingCoachingScoreStore(
        inner = inner,
        cache = newTtlCache<RollingCoachingScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling coaching scores. Empty fields match all. */
data class RollingCoachingScoreQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCoachingScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCoachingScore>): MutableList<RollingCoachingScore> {
        val matched = filter(items) { candidate: RollingCoachingScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCoachingScores(store: RollingCoachingScoreStore, query: RollingCoachingScoreQuery): MutableList<RollingCoachingScore> {
    return query.apply(store.list())
}
