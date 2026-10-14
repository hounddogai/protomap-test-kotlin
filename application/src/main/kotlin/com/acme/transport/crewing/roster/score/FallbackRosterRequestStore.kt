package com.acme.transport.crewing.roster.score

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback roster requests by code. */
interface FallbackRosterRequestStore {
    fun find(code: String): FallbackRosterRequest?
    fun save(item: FallbackRosterRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRosterRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRosterRequestStore(
    var items: MutableMap<String, FallbackRosterRequest>,
    var order: MutableList<String>,
) : FallbackRosterRequestStore {
    override fun find(code: String): FallbackRosterRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRosterRequest) {
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

    override fun list(): MutableList<FallbackRosterRequest> {
        val out = mutableListOf<FallbackRosterRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRosterRequestStore(): FallbackRosterRequestStore {
    return MemoryFallbackRosterRequestStore(
        mutableMapOf<String, FallbackRosterRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRosterRequestStore(
    var inner: FallbackRosterRequestStore,
    var cache: TtlCache<FallbackRosterRequest>,
    var hits: Int,
    var misses: Int,
) : FallbackRosterRequestStore {
    override fun find(code: String): FallbackRosterRequest? {
        val hit = (cache.get(code) as FallbackRosterRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRosterRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRosterRequest> = inner.list()
}

fun newCachedFallbackRosterRequestStore(inner: FallbackRosterRequestStore, ttl: Int, clock: Clock): FallbackRosterRequestStore {
    return CachedFallbackRosterRequestStore(
        inner = inner,
        cache = newTtlCache<FallbackRosterRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback roster requests. Empty fields match all. */
data class FallbackRosterRequestQuery(
    var depot: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRosterRequest): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRosterRequest>): MutableList<FallbackRosterRequest> {
        val matched = filter(items) { candidate: FallbackRosterRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRosterRequests(store: FallbackRosterRequestStore, query: FallbackRosterRequestQuery): MutableList<FallbackRosterRequest> {
    return query.apply(store.list())
}
