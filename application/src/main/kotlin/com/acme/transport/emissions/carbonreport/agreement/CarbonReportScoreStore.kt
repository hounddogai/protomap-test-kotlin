package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report scores by code. */
interface CarbonReportScoreStore {
    fun find(code: String): CarbonReportScore?
    fun save(item: CarbonReportScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportScoreStore(
    var items: MutableMap<String, CarbonReportScore>,
    var order: MutableList<String>,
) : CarbonReportScoreStore {
    override fun find(code: String): CarbonReportScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportScore) {
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

    override fun list(): MutableList<CarbonReportScore> {
        val out = mutableListOf<CarbonReportScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportScoreStore(): CarbonReportScoreStore {
    return MemoryCarbonReportScoreStore(mutableMapOf<String, CarbonReportScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportScoreStore(
    var inner: CarbonReportScoreStore,
    var cache: TtlCache<CarbonReportScore>,
    var hits: Int,
    var misses: Int,
) : CarbonReportScoreStore {
    override fun find(code: String): CarbonReportScore? {
        val hit = (cache.get(code) as CarbonReportScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportScore> = inner.list()
}

fun newCachedCarbonReportScoreStore(inner: CarbonReportScoreStore, ttl: Int, clock: Clock): CarbonReportScoreStore {
    return CachedCarbonReportScoreStore(
        inner = inner,
        cache = newTtlCache<CarbonReportScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report scores. Empty fields match all. */
data class CarbonReportScoreQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportScore>): MutableList<CarbonReportScore> {
        val matched = filter(items) { candidate: CarbonReportScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportScores(store: CarbonReportScoreStore, query: CarbonReportScoreQuery): MutableList<CarbonReportScore> {
    return query.apply(store.list())
}
