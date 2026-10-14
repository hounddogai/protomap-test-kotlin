package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional carbon report scores by code. */
interface RegionalCarbonReportScoreStore {
    fun find(code: String): RegionalCarbonReportScore?
    fun save(item: RegionalCarbonReportScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCarbonReportScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCarbonReportScoreStore(
    var items: MutableMap<String, RegionalCarbonReportScore>,
    var order: MutableList<String>,
) : RegionalCarbonReportScoreStore {
    override fun find(code: String): RegionalCarbonReportScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCarbonReportScore) {
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

    override fun list(): MutableList<RegionalCarbonReportScore> {
        val out = mutableListOf<RegionalCarbonReportScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCarbonReportScoreStore(): RegionalCarbonReportScoreStore {
    return MemoryRegionalCarbonReportScoreStore(
        mutableMapOf<String, RegionalCarbonReportScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCarbonReportScoreStore(
    var inner: RegionalCarbonReportScoreStore,
    var cache: TtlCache<RegionalCarbonReportScore>,
    var hits: Int,
    var misses: Int,
) : RegionalCarbonReportScoreStore {
    override fun find(code: String): RegionalCarbonReportScore? {
        val hit = (cache.get(code) as RegionalCarbonReportScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCarbonReportScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCarbonReportScore> = inner.list()
}

fun newCachedRegionalCarbonReportScoreStore(inner: RegionalCarbonReportScoreStore, ttl: Int, clock: Clock): RegionalCarbonReportScoreStore {
    return CachedRegionalCarbonReportScoreStore(
        inner = inner,
        cache = newTtlCache<RegionalCarbonReportScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional carbon report scores. Empty fields match all. */
data class RegionalCarbonReportScoreQuery(
    var lane: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCarbonReportScore): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCarbonReportScore>): MutableList<RegionalCarbonReportScore> {
        val matched = filter(items) { candidate: RegionalCarbonReportScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCarbonReportScores(store: RegionalCarbonReportScoreStore, query: RegionalCarbonReportScoreQuery): MutableList<RegionalCarbonReportScore> {
    return query.apply(store.list())
}
