package com.acme.transport.lastmile.locker.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped locker scores by code. */
interface CappedLockerScoreStore {
    fun find(code: String): CappedLockerScore?
    fun save(item: CappedLockerScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedLockerScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedLockerScoreStore(
    var items: MutableMap<String, CappedLockerScore>,
    var order: MutableList<String>,
) : CappedLockerScoreStore {
    override fun find(code: String): CappedLockerScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedLockerScore) {
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

    override fun list(): MutableList<CappedLockerScore> {
        val out = mutableListOf<CappedLockerScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedLockerScoreStore(): CappedLockerScoreStore {
    return MemoryCappedLockerScoreStore(mutableMapOf<String, CappedLockerScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedLockerScoreStore(
    var inner: CappedLockerScoreStore,
    var cache: TtlCache<CappedLockerScore>,
    var hits: Int,
    var misses: Int,
) : CappedLockerScoreStore {
    override fun find(code: String): CappedLockerScore? {
        val hit = (cache.get(code) as CappedLockerScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedLockerScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedLockerScore> = inner.list()
}

fun newCachedCappedLockerScoreStore(inner: CappedLockerScoreStore, ttl: Int, clock: Clock): CappedLockerScoreStore {
    return CachedCappedLockerScoreStore(
        inner = inner,
        cache = newTtlCache<CappedLockerScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped locker scores. Empty fields match all. */
data class CappedLockerScoreQuery(
    var category: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedLockerScore): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<CappedLockerScore>): MutableList<CappedLockerScore> {
        val matched = filter(items) { candidate: CappedLockerScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedLockerScores(store: CappedLockerScoreStore, query: CappedLockerScoreQuery): MutableList<CappedLockerScore> {
    return query.apply(store.list())
}
