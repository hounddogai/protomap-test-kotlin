package com.acme.transport.safety.coaching.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps seasonal coaching requests by code. */
interface SeasonalCoachingRequestStore {
    fun find(code: String): SeasonalCoachingRequest?
    fun save(item: SeasonalCoachingRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCoachingRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCoachingRequestStore(
    var items: MutableMap<String, SeasonalCoachingRequest>,
    var order: MutableList<String>,
) : SeasonalCoachingRequestStore {
    override fun find(code: String): SeasonalCoachingRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCoachingRequest) {
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

    override fun list(): MutableList<SeasonalCoachingRequest> {
        val out = mutableListOf<SeasonalCoachingRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCoachingRequestStore(): SeasonalCoachingRequestStore {
    return MemorySeasonalCoachingRequestStore(
        mutableMapOf<String, SeasonalCoachingRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCoachingRequestStore(
    var inner: SeasonalCoachingRequestStore,
    var cache: TtlCache<SeasonalCoachingRequest>,
    var hits: Int,
    var misses: Int,
) : SeasonalCoachingRequestStore {
    override fun find(code: String): SeasonalCoachingRequest? {
        val hit = (cache.get(code) as SeasonalCoachingRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCoachingRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCoachingRequest> = inner.list()
}

fun newCachedSeasonalCoachingRequestStore(inner: SeasonalCoachingRequestStore, ttl: Int, clock: Clock): SeasonalCoachingRequestStore {
    return CachedSeasonalCoachingRequestStore(
        inner = inner,
        cache = newTtlCache<SeasonalCoachingRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal coaching requests. Empty fields match all. */
data class SeasonalCoachingRequestQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCoachingRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalCoachingRequest>): MutableList<SeasonalCoachingRequest> {
        val matched = filter(items) { candidate: SeasonalCoachingRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCoachingRequests(store: SeasonalCoachingRequestStore, query: SeasonalCoachingRequestQuery): MutableList<SeasonalCoachingRequest> {
    return query.apply(store.list())
}
