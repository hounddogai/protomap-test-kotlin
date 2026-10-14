package com.acme.transport.emissions.idling.forecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling scores by code. */
interface IdlingScoreStore {
    fun find(code: String): IdlingScore?
    fun save(item: IdlingScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingScoreStore(
    var items: MutableMap<String, IdlingScore>,
    var order: MutableList<String>,
) : IdlingScoreStore {
    override fun find(code: String): IdlingScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingScore) {
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

    override fun list(): MutableList<IdlingScore> {
        val out = mutableListOf<IdlingScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingScoreStore(): IdlingScoreStore {
    return MemoryIdlingScoreStore(mutableMapOf<String, IdlingScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingScoreStore(
    var inner: IdlingScoreStore,
    var cache: TtlCache<IdlingScore>,
    var hits: Int,
    var misses: Int,
) : IdlingScoreStore {
    override fun find(code: String): IdlingScore? {
        val hit = (cache.get(code) as IdlingScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingScore> = inner.list()
}

fun newCachedIdlingScoreStore(inner: IdlingScoreStore, ttl: Int, clock: Clock): IdlingScoreStore {
    return CachedIdlingScoreStore(
        inner = inner,
        cache = newTtlCache<IdlingScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling scores. Empty fields match all. */
data class IdlingScoreQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingScore>): MutableList<IdlingScore> {
        val matched = filter(items) { candidate: IdlingScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingScores(store: IdlingScoreStore, query: IdlingScoreQuery): MutableList<IdlingScore> {
    return query.apply(store.list())
}
