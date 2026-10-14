package com.acme.transport.emissions.idling.score

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual idling scores by code. */
interface ManualIdlingScoreStore {
    fun find(code: String): ManualIdlingScore?
    fun save(item: ManualIdlingScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualIdlingScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualIdlingScoreStore(
    var items: MutableMap<String, ManualIdlingScore>,
    var order: MutableList<String>,
) : ManualIdlingScoreStore {
    override fun find(code: String): ManualIdlingScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualIdlingScore) {
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

    override fun list(): MutableList<ManualIdlingScore> {
        val out = mutableListOf<ManualIdlingScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualIdlingScoreStore(): ManualIdlingScoreStore {
    return MemoryManualIdlingScoreStore(mutableMapOf<String, ManualIdlingScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualIdlingScoreStore(
    var inner: ManualIdlingScoreStore,
    var cache: TtlCache<ManualIdlingScore>,
    var hits: Int,
    var misses: Int,
) : ManualIdlingScoreStore {
    override fun find(code: String): ManualIdlingScore? {
        val hit = (cache.get(code) as ManualIdlingScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualIdlingScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualIdlingScore> = inner.list()
}

fun newCachedManualIdlingScoreStore(inner: ManualIdlingScoreStore, ttl: Int, clock: Clock): ManualIdlingScoreStore {
    return CachedManualIdlingScoreStore(
        inner = inner,
        cache = newTtlCache<ManualIdlingScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual idling scores. Empty fields match all. */
data class ManualIdlingScoreQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualIdlingScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ManualIdlingScore>): MutableList<ManualIdlingScore> {
        val matched = filter(items) { candidate: ManualIdlingScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualIdlingScores(store: ManualIdlingScoreStore, query: ManualIdlingScoreQuery): MutableList<ManualIdlingScore> {
    return query.apply(store.list())
}
