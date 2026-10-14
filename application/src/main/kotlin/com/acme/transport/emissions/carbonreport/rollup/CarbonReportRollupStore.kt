package com.acme.transport.emissions.carbonreport.rollup

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report rollups by code. */
interface CarbonReportRollupStore {
    fun find(code: String): CarbonReportRollup?
    fun save(item: CarbonReportRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportRollupStore(
    var items: MutableMap<String, CarbonReportRollup>,
    var order: MutableList<String>,
) : CarbonReportRollupStore {
    override fun find(code: String): CarbonReportRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportRollup) {
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

    override fun list(): MutableList<CarbonReportRollup> {
        val out = mutableListOf<CarbonReportRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportRollupStore(): CarbonReportRollupStore {
    return MemoryCarbonReportRollupStore(mutableMapOf<String, CarbonReportRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportRollupStore(
    var inner: CarbonReportRollupStore,
    var cache: TtlCache<CarbonReportRollup>,
    var hits: Int,
    var misses: Int,
) : CarbonReportRollupStore {
    override fun find(code: String): CarbonReportRollup? {
        val hit = (cache.get(code) as CarbonReportRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportRollup> = inner.list()
}

fun newCachedCarbonReportRollupStore(inner: CarbonReportRollupStore, ttl: Int, clock: Clock): CarbonReportRollupStore {
    return CachedCarbonReportRollupStore(
        inner = inner,
        cache = newTtlCache<CarbonReportRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report rollups. Empty fields match all. */
data class CarbonReportRollupQuery(
    var lane: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportRollup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportRollup>): MutableList<CarbonReportRollup> {
        val matched = filter(items) { candidate: CarbonReportRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportRollups(store: CarbonReportRollupStore, query: CarbonReportRollupQuery): MutableList<CarbonReportRollup> {
    return query.apply(store.list())
}
