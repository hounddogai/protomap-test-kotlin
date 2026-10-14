package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling cross dock scores by code. */
interface RollingCrossDockScoreStore {
    fun find(code: String): RollingCrossDockScore?
    fun save(item: RollingCrossDockScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCrossDockScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCrossDockScoreStore(
    var items: MutableMap<String, RollingCrossDockScore>,
    var order: MutableList<String>,
) : RollingCrossDockScoreStore {
    override fun find(code: String): RollingCrossDockScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCrossDockScore) {
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

    override fun list(): MutableList<RollingCrossDockScore> {
        val out = mutableListOf<RollingCrossDockScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCrossDockScoreStore(): RollingCrossDockScoreStore {
    return MemoryRollingCrossDockScoreStore(
        mutableMapOf<String, RollingCrossDockScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCrossDockScoreStore(
    var inner: RollingCrossDockScoreStore,
    var cache: TtlCache<RollingCrossDockScore>,
    var hits: Int,
    var misses: Int,
) : RollingCrossDockScoreStore {
    override fun find(code: String): RollingCrossDockScore? {
        val hit = (cache.get(code) as RollingCrossDockScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCrossDockScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCrossDockScore> = inner.list()
}

fun newCachedRollingCrossDockScoreStore(inner: RollingCrossDockScoreStore, ttl: Int, clock: Clock): RollingCrossDockScoreStore {
    return CachedRollingCrossDockScoreStore(
        inner = inner,
        cache = newTtlCache<RollingCrossDockScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling cross dock scores. Empty fields match all. */
data class RollingCrossDockScoreQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCrossDockScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCrossDockScore>): MutableList<RollingCrossDockScore> {
        val matched = filter(items) { candidate: RollingCrossDockScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCrossDockScores(store: RollingCrossDockScoreStore, query: RollingCrossDockScoreQuery): MutableList<RollingCrossDockScore> {
    return query.apply(store.list())
}
