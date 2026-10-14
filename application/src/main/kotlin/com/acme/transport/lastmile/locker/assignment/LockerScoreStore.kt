package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker scores by code. */
interface LockerScoreStore {
    fun find(code: String): LockerScore?
    fun save(item: LockerScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerScoreStore(
    var items: MutableMap<String, LockerScore>,
    var order: MutableList<String>,
) : LockerScoreStore {
    override fun find(code: String): LockerScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerScore) {
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

    override fun list(): MutableList<LockerScore> {
        val out = mutableListOf<LockerScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerScoreStore(): LockerScoreStore {
    return MemoryLockerScoreStore(mutableMapOf<String, LockerScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerScoreStore(
    var inner: LockerScoreStore,
    var cache: TtlCache<LockerScore>,
    var hits: Int,
    var misses: Int,
) : LockerScoreStore {
    override fun find(code: String): LockerScore? {
        val hit = (cache.get(code) as LockerScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerScore> = inner.list()
}

fun newCachedLockerScoreStore(inner: LockerScoreStore, ttl: Int, clock: Clock): LockerScoreStore {
    return CachedLockerScoreStore(
        inner = inner,
        cache = newTtlCache<LockerScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker scores. Empty fields match all. */
data class LockerScoreQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerScore>): MutableList<LockerScore> {
        val matched = filter(items) { candidate: LockerScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerScores(store: LockerScoreStore, query: LockerScoreQuery): MutableList<LockerScore> {
    return query.apply(store.list())
}
