package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional node scores by code. */
interface RegionalNodeScoreStore {
    fun find(code: String): RegionalNodeScore?
    fun save(item: RegionalNodeScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalNodeScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalNodeScoreStore(
    var items: MutableMap<String, RegionalNodeScore>,
    var order: MutableList<String>,
) : RegionalNodeScoreStore {
    override fun find(code: String): RegionalNodeScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalNodeScore) {
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

    override fun list(): MutableList<RegionalNodeScore> {
        val out = mutableListOf<RegionalNodeScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalNodeScoreStore(): RegionalNodeScoreStore {
    return MemoryRegionalNodeScoreStore(mutableMapOf<String, RegionalNodeScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalNodeScoreStore(
    var inner: RegionalNodeScoreStore,
    var cache: TtlCache<RegionalNodeScore>,
    var hits: Int,
    var misses: Int,
) : RegionalNodeScoreStore {
    override fun find(code: String): RegionalNodeScore? {
        val hit = (cache.get(code) as RegionalNodeScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalNodeScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalNodeScore> = inner.list()
}

fun newCachedRegionalNodeScoreStore(inner: RegionalNodeScoreStore, ttl: Int, clock: Clock): RegionalNodeScoreStore {
    return CachedRegionalNodeScoreStore(
        inner = inner,
        cache = newTtlCache<RegionalNodeScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional node scores. Empty fields match all. */
data class RegionalNodeScoreQuery(
    var depot: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalNodeScore): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalNodeScore>): MutableList<RegionalNodeScore> {
        val matched = filter(items) { candidate: RegionalNodeScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalNodeScores(store: RegionalNodeScoreStore, query: RegionalNodeScoreQuery): MutableList<RegionalNodeScore> {
    return query.apply(store.list())
}
