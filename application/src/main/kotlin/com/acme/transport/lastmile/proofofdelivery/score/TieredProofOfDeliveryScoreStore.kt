package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered proof of delivery scores by code. */
interface TieredProofOfDeliveryScoreStore {
    fun find(code: String): TieredProofOfDeliveryScore?
    fun save(item: TieredProofOfDeliveryScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredProofOfDeliveryScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredProofOfDeliveryScoreStore(
    var items: MutableMap<String, TieredProofOfDeliveryScore>,
    var order: MutableList<String>,
) : TieredProofOfDeliveryScoreStore {
    override fun find(code: String): TieredProofOfDeliveryScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredProofOfDeliveryScore) {
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

    override fun list(): MutableList<TieredProofOfDeliveryScore> {
        val out = mutableListOf<TieredProofOfDeliveryScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredProofOfDeliveryScoreStore(): TieredProofOfDeliveryScoreStore {
    return MemoryTieredProofOfDeliveryScoreStore(
        mutableMapOf<String, TieredProofOfDeliveryScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredProofOfDeliveryScoreStore(
    var inner: TieredProofOfDeliveryScoreStore,
    var cache: TtlCache<TieredProofOfDeliveryScore>,
    var hits: Int,
    var misses: Int,
) : TieredProofOfDeliveryScoreStore {
    override fun find(code: String): TieredProofOfDeliveryScore? {
        val hit = (cache.get(code) as TieredProofOfDeliveryScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredProofOfDeliveryScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredProofOfDeliveryScore> = inner.list()
}

fun newCachedTieredProofOfDeliveryScoreStore(inner: TieredProofOfDeliveryScoreStore, ttl: Int, clock: Clock): TieredProofOfDeliveryScoreStore {
    return CachedTieredProofOfDeliveryScoreStore(
        inner = inner,
        cache = newTtlCache<TieredProofOfDeliveryScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered proof of delivery scores. Empty fields match all. */
data class TieredProofOfDeliveryScoreQuery(
    var lane: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredProofOfDeliveryScore): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<TieredProofOfDeliveryScore>): MutableList<TieredProofOfDeliveryScore> {
        val matched = filter(items) { candidate: TieredProofOfDeliveryScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredProofOfDeliveryScores(store: TieredProofOfDeliveryScoreStore, query: TieredProofOfDeliveryScoreQuery): MutableList<TieredProofOfDeliveryScore> {
    return query.apply(store.list())
}
