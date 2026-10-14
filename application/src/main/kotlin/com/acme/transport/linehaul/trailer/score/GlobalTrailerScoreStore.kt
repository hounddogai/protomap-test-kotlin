package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global trailer scores by code. */
interface GlobalTrailerScoreStore {
    fun find(code: String): GlobalTrailerScore?
    fun save(item: GlobalTrailerScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTrailerScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTrailerScoreStore(
    var items: MutableMap<String, GlobalTrailerScore>,
    var order: MutableList<String>,
) : GlobalTrailerScoreStore {
    override fun find(code: String): GlobalTrailerScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTrailerScore) {
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

    override fun list(): MutableList<GlobalTrailerScore> {
        val out = mutableListOf<GlobalTrailerScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTrailerScoreStore(): GlobalTrailerScoreStore {
    return MemoryGlobalTrailerScoreStore(mutableMapOf<String, GlobalTrailerScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTrailerScoreStore(
    var inner: GlobalTrailerScoreStore,
    var cache: TtlCache<GlobalTrailerScore>,
    var hits: Int,
    var misses: Int,
) : GlobalTrailerScoreStore {
    override fun find(code: String): GlobalTrailerScore? {
        val hit = (cache.get(code) as GlobalTrailerScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTrailerScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTrailerScore> = inner.list()
}

fun newCachedGlobalTrailerScoreStore(inner: GlobalTrailerScoreStore, ttl: Int, clock: Clock): GlobalTrailerScoreStore {
    return CachedGlobalTrailerScoreStore(
        inner = inner,
        cache = newTtlCache<GlobalTrailerScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global trailer scores. Empty fields match all. */
data class GlobalTrailerScoreQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTrailerScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalTrailerScore>): MutableList<GlobalTrailerScore> {
        val matched = filter(items) { candidate: GlobalTrailerScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTrailerScores(store: GlobalTrailerScoreStore, query: GlobalTrailerScoreQuery): MutableList<GlobalTrailerScore> {
    return query.apply(store.list())
}
