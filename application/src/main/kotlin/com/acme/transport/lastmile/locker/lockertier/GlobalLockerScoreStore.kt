package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global locker scores by code. */
interface GlobalLockerScoreStore {
    fun find(code: String): GlobalLockerScore?
    fun save(item: GlobalLockerScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalLockerScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalLockerScoreStore(
    var items: MutableMap<String, GlobalLockerScore>,
    var order: MutableList<String>,
) : GlobalLockerScoreStore {
    override fun find(code: String): GlobalLockerScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalLockerScore) {
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

    override fun list(): MutableList<GlobalLockerScore> {
        val out = mutableListOf<GlobalLockerScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalLockerScoreStore(): GlobalLockerScoreStore {
    return MemoryGlobalLockerScoreStore(mutableMapOf<String, GlobalLockerScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalLockerScoreStore(
    var inner: GlobalLockerScoreStore,
    var cache: TtlCache<GlobalLockerScore>,
    var hits: Int,
    var misses: Int,
) : GlobalLockerScoreStore {
    override fun find(code: String): GlobalLockerScore? {
        val hit = (cache.get(code) as GlobalLockerScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalLockerScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalLockerScore> = inner.list()
}

fun newCachedGlobalLockerScoreStore(inner: GlobalLockerScoreStore, ttl: Int, clock: Clock): GlobalLockerScoreStore {
    return CachedGlobalLockerScoreStore(
        inner = inner,
        cache = newTtlCache<GlobalLockerScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global locker scores. Empty fields match all. */
data class GlobalLockerScoreQuery(
    var category: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalLockerScore): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalLockerScore>): MutableList<GlobalLockerScore> {
        val matched = filter(items) { candidate: GlobalLockerScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalLockerScores(store: GlobalLockerScoreStore, query: GlobalLockerScoreQuery): MutableList<GlobalLockerScore> {
    return query.apply(store.list())
}
