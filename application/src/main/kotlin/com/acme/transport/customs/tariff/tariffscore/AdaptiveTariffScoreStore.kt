package com.acme.transport.customs.tariff.tariffscore

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive tariff scores by code. */
interface AdaptiveTariffScoreStore {
    fun find(code: String): AdaptiveTariffScore?
    fun save(item: AdaptiveTariffScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTariffScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTariffScoreStore(
    var items: MutableMap<String, AdaptiveTariffScore>,
    var order: MutableList<String>,
) : AdaptiveTariffScoreStore {
    override fun find(code: String): AdaptiveTariffScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTariffScore) {
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

    override fun list(): MutableList<AdaptiveTariffScore> {
        val out = mutableListOf<AdaptiveTariffScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTariffScoreStore(): AdaptiveTariffScoreStore {
    return MemoryAdaptiveTariffScoreStore(mutableMapOf<String, AdaptiveTariffScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTariffScoreStore(
    var inner: AdaptiveTariffScoreStore,
    var cache: TtlCache<AdaptiveTariffScore>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTariffScoreStore {
    override fun find(code: String): AdaptiveTariffScore? {
        val hit = (cache.get(code) as AdaptiveTariffScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTariffScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTariffScore> = inner.list()
}

fun newCachedAdaptiveTariffScoreStore(inner: AdaptiveTariffScoreStore, ttl: Int, clock: Clock): AdaptiveTariffScoreStore {
    return CachedAdaptiveTariffScoreStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTariffScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive tariff scores. Empty fields match all. */
data class AdaptiveTariffScoreQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTariffScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTariffScore>): MutableList<AdaptiveTariffScore> {
        val matched = filter(items) { candidate: AdaptiveTariffScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTariffScores(store: AdaptiveTariffScoreStore, query: AdaptiveTariffScoreQuery): MutableList<AdaptiveTariffScore> {
    return query.apply(store.list())
}
