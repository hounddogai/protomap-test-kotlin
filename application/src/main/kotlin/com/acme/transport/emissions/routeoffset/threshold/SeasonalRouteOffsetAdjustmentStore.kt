package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal route offset adjustments by code. */
interface SeasonalRouteOffsetAdjustmentStore {
    fun find(code: String): SeasonalRouteOffsetAdjustment?
    fun save(item: SeasonalRouteOffsetAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRouteOffsetAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRouteOffsetAdjustmentStore(
    var items: MutableMap<String, SeasonalRouteOffsetAdjustment>,
    var order: MutableList<String>,
) : SeasonalRouteOffsetAdjustmentStore {
    override fun find(code: String): SeasonalRouteOffsetAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRouteOffsetAdjustment) {
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

    override fun list(): MutableList<SeasonalRouteOffsetAdjustment> {
        val out = mutableListOf<SeasonalRouteOffsetAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRouteOffsetAdjustmentStore(): SeasonalRouteOffsetAdjustmentStore {
    return MemorySeasonalRouteOffsetAdjustmentStore(
        mutableMapOf<String, SeasonalRouteOffsetAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRouteOffsetAdjustmentStore(
    var inner: SeasonalRouteOffsetAdjustmentStore,
    var cache: TtlCache<SeasonalRouteOffsetAdjustment>,
    var hits: Int,
    var misses: Int,
) : SeasonalRouteOffsetAdjustmentStore {
    override fun find(code: String): SeasonalRouteOffsetAdjustment? {
        val hit = (cache.get(code) as SeasonalRouteOffsetAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRouteOffsetAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRouteOffsetAdjustment> = inner.list()
}

fun newCachedSeasonalRouteOffsetAdjustmentStore(inner: SeasonalRouteOffsetAdjustmentStore, ttl: Int, clock: Clock): SeasonalRouteOffsetAdjustmentStore {
    return CachedSeasonalRouteOffsetAdjustmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalRouteOffsetAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal route offset adjustments. Empty fields match all. */
data class SeasonalRouteOffsetAdjustmentQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRouteOffsetAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRouteOffsetAdjustment>): MutableList<SeasonalRouteOffsetAdjustment> {
        val matched = filter(items) { candidate: SeasonalRouteOffsetAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRouteOffsetAdjustments(store: SeasonalRouteOffsetAdjustmentStore, query: SeasonalRouteOffsetAdjustmentQuery): MutableList<SeasonalRouteOffsetAdjustment> {
    return query.apply(store.list())
}
