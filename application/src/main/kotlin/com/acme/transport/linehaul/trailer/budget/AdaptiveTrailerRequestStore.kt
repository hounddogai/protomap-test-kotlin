package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive trailer requests by code. */
interface AdaptiveTrailerRequestStore {
    fun find(code: String): AdaptiveTrailerRequest?
    fun save(item: AdaptiveTrailerRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTrailerRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTrailerRequestStore(
    var items: MutableMap<String, AdaptiveTrailerRequest>,
    var order: MutableList<String>,
) : AdaptiveTrailerRequestStore {
    override fun find(code: String): AdaptiveTrailerRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTrailerRequest) {
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

    override fun list(): MutableList<AdaptiveTrailerRequest> {
        val out = mutableListOf<AdaptiveTrailerRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTrailerRequestStore(): AdaptiveTrailerRequestStore {
    return MemoryAdaptiveTrailerRequestStore(
        mutableMapOf<String, AdaptiveTrailerRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTrailerRequestStore(
    var inner: AdaptiveTrailerRequestStore,
    var cache: TtlCache<AdaptiveTrailerRequest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTrailerRequestStore {
    override fun find(code: String): AdaptiveTrailerRequest? {
        val hit = (cache.get(code) as AdaptiveTrailerRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTrailerRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTrailerRequest> = inner.list()
}

fun newCachedAdaptiveTrailerRequestStore(inner: AdaptiveTrailerRequestStore, ttl: Int, clock: Clock): AdaptiveTrailerRequestStore {
    return CachedAdaptiveTrailerRequestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTrailerRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive trailer requests. Empty fields match all. */
data class AdaptiveTrailerRequestQuery(
    var lane: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTrailerRequest): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<AdaptiveTrailerRequest>): MutableList<AdaptiveTrailerRequest> {
        val matched = filter(items) { candidate: AdaptiveTrailerRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTrailerRequests(store: AdaptiveTrailerRequestStore, query: AdaptiveTrailerRequestQuery): MutableList<AdaptiveTrailerRequest> {
    return query.apply(store.list())
}
