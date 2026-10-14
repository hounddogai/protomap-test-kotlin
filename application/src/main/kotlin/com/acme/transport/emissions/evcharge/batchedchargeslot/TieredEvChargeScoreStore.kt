package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered ev charge scores by code. */
interface TieredEvChargeScoreStore {
    fun find(code: String): TieredEvChargeScore?
    fun save(item: TieredEvChargeScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredEvChargeScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredEvChargeScoreStore(
    var items: MutableMap<String, TieredEvChargeScore>,
    var order: MutableList<String>,
) : TieredEvChargeScoreStore {
    override fun find(code: String): TieredEvChargeScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredEvChargeScore) {
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

    override fun list(): MutableList<TieredEvChargeScore> {
        val out = mutableListOf<TieredEvChargeScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredEvChargeScoreStore(): TieredEvChargeScoreStore {
    return MemoryTieredEvChargeScoreStore(mutableMapOf<String, TieredEvChargeScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredEvChargeScoreStore(
    var inner: TieredEvChargeScoreStore,
    var cache: TtlCache<TieredEvChargeScore>,
    var hits: Int,
    var misses: Int,
) : TieredEvChargeScoreStore {
    override fun find(code: String): TieredEvChargeScore? {
        val hit = (cache.get(code) as TieredEvChargeScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredEvChargeScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredEvChargeScore> = inner.list()
}

fun newCachedTieredEvChargeScoreStore(inner: TieredEvChargeScoreStore, ttl: Int, clock: Clock): TieredEvChargeScoreStore {
    return CachedTieredEvChargeScoreStore(
        inner = inner,
        cache = newTtlCache<TieredEvChargeScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered ev charge scores. Empty fields match all. */
data class TieredEvChargeScoreQuery(
    var unit: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredEvChargeScore): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<TieredEvChargeScore>): MutableList<TieredEvChargeScore> {
        val matched = filter(items) { candidate: TieredEvChargeScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredEvChargeScores(store: TieredEvChargeScoreStore, query: TieredEvChargeScoreQuery): MutableList<TieredEvChargeScore> {
    return query.apply(store.list())
}
