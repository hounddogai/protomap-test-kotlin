package com.acme.transport.network.node.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling node scores by code. */
interface RollingNodeScoreStore {
    fun find(code: String): RollingNodeScore?
    fun save(item: RollingNodeScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingNodeScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingNodeScoreStore(
    var items: MutableMap<String, RollingNodeScore>,
    var order: MutableList<String>,
) : RollingNodeScoreStore {
    override fun find(code: String): RollingNodeScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingNodeScore) {
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

    override fun list(): MutableList<RollingNodeScore> {
        val out = mutableListOf<RollingNodeScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingNodeScoreStore(): RollingNodeScoreStore {
    return MemoryRollingNodeScoreStore(mutableMapOf<String, RollingNodeScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingNodeScoreStore(
    var inner: RollingNodeScoreStore,
    var cache: TtlCache<RollingNodeScore>,
    var hits: Int,
    var misses: Int,
) : RollingNodeScoreStore {
    override fun find(code: String): RollingNodeScore? {
        val hit = (cache.get(code) as RollingNodeScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingNodeScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingNodeScore> = inner.list()
}

fun newCachedRollingNodeScoreStore(inner: RollingNodeScoreStore, ttl: Int, clock: Clock): RollingNodeScoreStore {
    return CachedRollingNodeScoreStore(
        inner = inner,
        cache = newTtlCache<RollingNodeScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling node scores. Empty fields match all. */
data class RollingNodeScoreQuery(
    var locale: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingNodeScore): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingNodeScore>): MutableList<RollingNodeScore> {
        val matched = filter(items) { candidate: RollingNodeScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingNodeScores(store: RollingNodeScoreStore, query: RollingNodeScoreQuery): MutableList<RollingNodeScore> {
    return query.apply(store.list())
}
