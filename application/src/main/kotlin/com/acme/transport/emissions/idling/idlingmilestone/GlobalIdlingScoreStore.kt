package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global idling scores by code. */
interface GlobalIdlingScoreStore {
    fun find(code: String): GlobalIdlingScore?
    fun save(item: GlobalIdlingScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalIdlingScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalIdlingScoreStore(
    var items: MutableMap<String, GlobalIdlingScore>,
    var order: MutableList<String>,
) : GlobalIdlingScoreStore {
    override fun find(code: String): GlobalIdlingScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalIdlingScore) {
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

    override fun list(): MutableList<GlobalIdlingScore> {
        val out = mutableListOf<GlobalIdlingScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalIdlingScoreStore(): GlobalIdlingScoreStore {
    return MemoryGlobalIdlingScoreStore(mutableMapOf<String, GlobalIdlingScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalIdlingScoreStore(
    var inner: GlobalIdlingScoreStore,
    var cache: TtlCache<GlobalIdlingScore>,
    var hits: Int,
    var misses: Int,
) : GlobalIdlingScoreStore {
    override fun find(code: String): GlobalIdlingScore? {
        val hit = (cache.get(code) as GlobalIdlingScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalIdlingScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalIdlingScore> = inner.list()
}

fun newCachedGlobalIdlingScoreStore(inner: GlobalIdlingScoreStore, ttl: Int, clock: Clock): GlobalIdlingScoreStore {
    return CachedGlobalIdlingScoreStore(
        inner = inner,
        cache = newTtlCache<GlobalIdlingScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global idling scores. Empty fields match all. */
data class GlobalIdlingScoreQuery(
    var region: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalIdlingScore): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalIdlingScore>): MutableList<GlobalIdlingScore> {
        val matched = filter(items) { candidate: GlobalIdlingScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalIdlingScores(store: GlobalIdlingScoreStore, query: GlobalIdlingScoreQuery): MutableList<GlobalIdlingScore> {
    return query.apply(store.list())
}
