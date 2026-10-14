package com.acme.transport.network.transittime.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time requests by code. */
interface TransitTimeRequestStore {
    fun find(code: String): TransitTimeRequest?
    fun save(item: TransitTimeRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeRequestStore(
    var items: MutableMap<String, TransitTimeRequest>,
    var order: MutableList<String>,
) : TransitTimeRequestStore {
    override fun find(code: String): TransitTimeRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeRequest) {
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

    override fun list(): MutableList<TransitTimeRequest> {
        val out = mutableListOf<TransitTimeRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeRequestStore(): TransitTimeRequestStore {
    return MemoryTransitTimeRequestStore(mutableMapOf<String, TransitTimeRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeRequestStore(
    var inner: TransitTimeRequestStore,
    var cache: TtlCache<TransitTimeRequest>,
    var hits: Int,
    var misses: Int,
) : TransitTimeRequestStore {
    override fun find(code: String): TransitTimeRequest? {
        val hit = (cache.get(code) as TransitTimeRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeRequest> = inner.list()
}

fun newCachedTransitTimeRequestStore(inner: TransitTimeRequestStore, ttl: Int, clock: Clock): TransitTimeRequestStore {
    return CachedTransitTimeRequestStore(
        inner = inner,
        cache = newTtlCache<TransitTimeRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time requests. Empty fields match all. */
data class TransitTimeRequestQuery(
    var category: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeRequest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<TransitTimeRequest>): MutableList<TransitTimeRequest> {
        val matched = filter(items) { candidate: TransitTimeRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeRequests(store: TransitTimeRequestStore, query: TransitTimeRequestQuery): MutableList<TransitTimeRequest> {
    return query.apply(store.list())
}
