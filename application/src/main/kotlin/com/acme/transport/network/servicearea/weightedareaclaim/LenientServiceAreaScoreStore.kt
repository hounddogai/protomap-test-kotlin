package com.acme.transport.network.servicearea.weightedareaclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient service area scores by code. */
interface LenientServiceAreaScoreStore {
    fun find(code: String): LenientServiceAreaScore?
    fun save(item: LenientServiceAreaScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientServiceAreaScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientServiceAreaScoreStore(
    var items: MutableMap<String, LenientServiceAreaScore>,
    var order: MutableList<String>,
) : LenientServiceAreaScoreStore {
    override fun find(code: String): LenientServiceAreaScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientServiceAreaScore) {
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

    override fun list(): MutableList<LenientServiceAreaScore> {
        val out = mutableListOf<LenientServiceAreaScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientServiceAreaScoreStore(): LenientServiceAreaScoreStore {
    return MemoryLenientServiceAreaScoreStore(
        mutableMapOf<String, LenientServiceAreaScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientServiceAreaScoreStore(
    var inner: LenientServiceAreaScoreStore,
    var cache: TtlCache<LenientServiceAreaScore>,
    var hits: Int,
    var misses: Int,
) : LenientServiceAreaScoreStore {
    override fun find(code: String): LenientServiceAreaScore? {
        val hit = (cache.get(code) as LenientServiceAreaScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientServiceAreaScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientServiceAreaScore> = inner.list()
}

fun newCachedLenientServiceAreaScoreStore(inner: LenientServiceAreaScoreStore, ttl: Int, clock: Clock): LenientServiceAreaScoreStore {
    return CachedLenientServiceAreaScoreStore(
        inner = inner,
        cache = newTtlCache<LenientServiceAreaScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient service area scores. Empty fields match all. */
data class LenientServiceAreaScoreQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientServiceAreaScore): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientServiceAreaScore>): MutableList<LenientServiceAreaScore> {
        val matched = filter(items) { candidate: LenientServiceAreaScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientServiceAreaScores(store: LenientServiceAreaScoreStore, query: LenientServiceAreaScoreQuery): MutableList<LenientServiceAreaScore> {
    return query.apply(store.list())
}
