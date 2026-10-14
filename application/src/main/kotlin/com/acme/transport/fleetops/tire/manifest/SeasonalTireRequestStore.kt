package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal tire requests by code. */
interface SeasonalTireRequestStore {
    fun find(code: String): SeasonalTireRequest?
    fun save(item: SeasonalTireRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTireRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTireRequestStore(
    var items: MutableMap<String, SeasonalTireRequest>,
    var order: MutableList<String>,
) : SeasonalTireRequestStore {
    override fun find(code: String): SeasonalTireRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTireRequest) {
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

    override fun list(): MutableList<SeasonalTireRequest> {
        val out = mutableListOf<SeasonalTireRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTireRequestStore(): SeasonalTireRequestStore {
    return MemorySeasonalTireRequestStore(mutableMapOf<String, SeasonalTireRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTireRequestStore(
    var inner: SeasonalTireRequestStore,
    var cache: TtlCache<SeasonalTireRequest>,
    var hits: Int,
    var misses: Int,
) : SeasonalTireRequestStore {
    override fun find(code: String): SeasonalTireRequest? {
        val hit = (cache.get(code) as SeasonalTireRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTireRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTireRequest> = inner.list()
}

fun newCachedSeasonalTireRequestStore(inner: SeasonalTireRequestStore, ttl: Int, clock: Clock): SeasonalTireRequestStore {
    return CachedSeasonalTireRequestStore(
        inner = inner,
        cache = newTtlCache<SeasonalTireRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal tire requests. Empty fields match all. */
data class SeasonalTireRequestQuery(
    var channel: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTireRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTireRequest>): MutableList<SeasonalTireRequest> {
        val matched = filter(items) { candidate: SeasonalTireRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTireRequests(store: SeasonalTireRequestStore, query: SeasonalTireRequestQuery): MutableList<SeasonalTireRequest> {
    return query.apply(store.list())
}
