package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive carbon report scores by code. */
interface AdaptiveCarbonReportScoreStore {
    fun find(code: String): AdaptiveCarbonReportScore?
    fun save(item: AdaptiveCarbonReportScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCarbonReportScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCarbonReportScoreStore(
    var items: MutableMap<String, AdaptiveCarbonReportScore>,
    var order: MutableList<String>,
) : AdaptiveCarbonReportScoreStore {
    override fun find(code: String): AdaptiveCarbonReportScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCarbonReportScore) {
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

    override fun list(): MutableList<AdaptiveCarbonReportScore> {
        val out = mutableListOf<AdaptiveCarbonReportScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCarbonReportScoreStore(): AdaptiveCarbonReportScoreStore {
    return MemoryAdaptiveCarbonReportScoreStore(
        mutableMapOf<String, AdaptiveCarbonReportScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCarbonReportScoreStore(
    var inner: AdaptiveCarbonReportScoreStore,
    var cache: TtlCache<AdaptiveCarbonReportScore>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCarbonReportScoreStore {
    override fun find(code: String): AdaptiveCarbonReportScore? {
        val hit = (cache.get(code) as AdaptiveCarbonReportScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCarbonReportScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCarbonReportScore> = inner.list()
}

fun newCachedAdaptiveCarbonReportScoreStore(inner: AdaptiveCarbonReportScoreStore, ttl: Int, clock: Clock): AdaptiveCarbonReportScoreStore {
    return CachedAdaptiveCarbonReportScoreStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCarbonReportScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive carbon report scores. Empty fields match all. */
data class AdaptiveCarbonReportScoreQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCarbonReportScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveCarbonReportScore>): MutableList<AdaptiveCarbonReportScore> {
        val matched = filter(items) { candidate: AdaptiveCarbonReportScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCarbonReportScores(store: AdaptiveCarbonReportScoreStore, query: AdaptiveCarbonReportScoreQuery): MutableList<AdaptiveCarbonReportScore> {
    return query.apply(store.list())
}
