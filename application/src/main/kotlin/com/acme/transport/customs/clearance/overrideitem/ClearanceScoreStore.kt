package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance scores by code. */
interface ClearanceScoreStore {
    fun find(code: String): ClearanceScore?
    fun save(item: ClearanceScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceScoreStore(
    var items: MutableMap<String, ClearanceScore>,
    var order: MutableList<String>,
) : ClearanceScoreStore {
    override fun find(code: String): ClearanceScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceScore) {
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

    override fun list(): MutableList<ClearanceScore> {
        val out = mutableListOf<ClearanceScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceScoreStore(): ClearanceScoreStore {
    return MemoryClearanceScoreStore(mutableMapOf<String, ClearanceScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceScoreStore(
    var inner: ClearanceScoreStore,
    var cache: TtlCache<ClearanceScore>,
    var hits: Int,
    var misses: Int,
) : ClearanceScoreStore {
    override fun find(code: String): ClearanceScore? {
        val hit = (cache.get(code) as ClearanceScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceScore> = inner.list()
}

fun newCachedClearanceScoreStore(inner: ClearanceScoreStore, ttl: Int, clock: Clock): ClearanceScoreStore {
    return CachedClearanceScoreStore(
        inner = inner,
        cache = newTtlCache<ClearanceScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance scores. Empty fields match all. */
data class ClearanceScoreQuery(
    var segment: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceScore): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceScore>): MutableList<ClearanceScore> {
        val matched = filter(items) { candidate: ClearanceScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceScores(store: ClearanceScoreStore, query: ClearanceScoreQuery): MutableList<ClearanceScore> {
    return query.apply(store.list())
}
