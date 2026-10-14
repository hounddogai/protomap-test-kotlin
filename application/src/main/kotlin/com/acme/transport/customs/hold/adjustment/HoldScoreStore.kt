package com.acme.transport.customs.hold.adjustment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold scores by code. */
interface HoldScoreStore {
    fun find(code: String): HoldScore?
    fun save(item: HoldScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldScoreStore(
    var items: MutableMap<String, HoldScore>,
    var order: MutableList<String>,
) : HoldScoreStore {
    override fun find(code: String): HoldScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldScore) {
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

    override fun list(): MutableList<HoldScore> {
        val out = mutableListOf<HoldScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldScoreStore(): HoldScoreStore {
    return MemoryHoldScoreStore(mutableMapOf<String, HoldScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldScoreStore(
    var inner: HoldScoreStore,
    var cache: TtlCache<HoldScore>,
    var hits: Int,
    var misses: Int,
) : HoldScoreStore {
    override fun find(code: String): HoldScore? {
        val hit = (cache.get(code) as HoldScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldScore> = inner.list()
}

fun newCachedHoldScoreStore(inner: HoldScoreStore, ttl: Int, clock: Clock): HoldScoreStore {
    return CachedHoldScoreStore(
        inner = inner,
        cache = newTtlCache<HoldScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold scores. Empty fields match all. */
data class HoldScoreQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldScore>): MutableList<HoldScore> {
        val matched = filter(items) { candidate: HoldScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldScores(store: HoldScoreStore, query: HoldScoreQuery): MutableList<HoldScore> {
    return query.apply(store.list())
}
