package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal route offset thresholds by code. */
interface SeasonalRouteOffsetThresholdStore {
    fun find(code: String): SeasonalRouteOffsetThreshold?
    fun save(item: SeasonalRouteOffsetThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRouteOffsetThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRouteOffsetThresholdStore(
    var items: MutableMap<String, SeasonalRouteOffsetThreshold>,
    var order: MutableList<String>,
) : SeasonalRouteOffsetThresholdStore {
    override fun find(code: String): SeasonalRouteOffsetThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRouteOffsetThreshold) {
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

    override fun list(): MutableList<SeasonalRouteOffsetThreshold> {
        val out = mutableListOf<SeasonalRouteOffsetThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRouteOffsetThresholdStore(): SeasonalRouteOffsetThresholdStore {
    return MemorySeasonalRouteOffsetThresholdStore(
        mutableMapOf<String, SeasonalRouteOffsetThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRouteOffsetThresholdStore(
    var inner: SeasonalRouteOffsetThresholdStore,
    var cache: TtlCache<SeasonalRouteOffsetThreshold>,
    var hits: Int,
    var misses: Int,
) : SeasonalRouteOffsetThresholdStore {
    override fun find(code: String): SeasonalRouteOffsetThreshold? {
        val hit = (cache.get(code) as SeasonalRouteOffsetThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRouteOffsetThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRouteOffsetThreshold> = inner.list()
}

fun newCachedSeasonalRouteOffsetThresholdStore(inner: SeasonalRouteOffsetThresholdStore, ttl: Int, clock: Clock): SeasonalRouteOffsetThresholdStore {
    return CachedSeasonalRouteOffsetThresholdStore(
        inner = inner,
        cache = newTtlCache<SeasonalRouteOffsetThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal route offset thresholds. Empty fields match all. */
data class SeasonalRouteOffsetThresholdQuery(
    var channel: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRouteOffsetThreshold): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRouteOffsetThreshold>): MutableList<SeasonalRouteOffsetThreshold> {
        val matched = filter(items) { candidate: SeasonalRouteOffsetThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRouteOffsetThresholds(store: SeasonalRouteOffsetThresholdStore, query: SeasonalRouteOffsetThresholdQuery): MutableList<SeasonalRouteOffsetThreshold> {
    return query.apply(store.list())
}
