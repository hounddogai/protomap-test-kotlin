package com.acme.transport.crewing.roster.forecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal roster estimates by code. */
interface SeasonalRosterEstimateStore {
    fun find(code: String): SeasonalRosterEstimate?
    fun save(item: SeasonalRosterEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRosterEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRosterEstimateStore(
    var items: MutableMap<String, SeasonalRosterEstimate>,
    var order: MutableList<String>,
) : SeasonalRosterEstimateStore {
    override fun find(code: String): SeasonalRosterEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRosterEstimate) {
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

    override fun list(): MutableList<SeasonalRosterEstimate> {
        val out = mutableListOf<SeasonalRosterEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRosterEstimateStore(): SeasonalRosterEstimateStore {
    return MemorySeasonalRosterEstimateStore(
        mutableMapOf<String, SeasonalRosterEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRosterEstimateStore(
    var inner: SeasonalRosterEstimateStore,
    var cache: TtlCache<SeasonalRosterEstimate>,
    var hits: Int,
    var misses: Int,
) : SeasonalRosterEstimateStore {
    override fun find(code: String): SeasonalRosterEstimate? {
        val hit = (cache.get(code) as SeasonalRosterEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRosterEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRosterEstimate> = inner.list()
}

fun newCachedSeasonalRosterEstimateStore(inner: SeasonalRosterEstimateStore, ttl: Int, clock: Clock): SeasonalRosterEstimateStore {
    return CachedSeasonalRosterEstimateStore(
        inner = inner,
        cache = newTtlCache<SeasonalRosterEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal roster estimates. Empty fields match all. */
data class SeasonalRosterEstimateQuery(
    var lane: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRosterEstimate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRosterEstimate>): MutableList<SeasonalRosterEstimate> {
        val matched = filter(items) { candidate: SeasonalRosterEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRosterEstimates(store: SeasonalRosterEstimateStore, query: SeasonalRosterEstimateQuery): MutableList<SeasonalRosterEstimate> {
    return query.apply(store.list())
}
