package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback attempt requests by code. */
interface FallbackAttemptRequestStore {
    fun find(code: String): FallbackAttemptRequest?
    fun save(item: FallbackAttemptRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAttemptRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAttemptRequestStore(
    var items: MutableMap<String, FallbackAttemptRequest>,
    var order: MutableList<String>,
) : FallbackAttemptRequestStore {
    override fun find(code: String): FallbackAttemptRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAttemptRequest) {
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

    override fun list(): MutableList<FallbackAttemptRequest> {
        val out = mutableListOf<FallbackAttemptRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAttemptRequestStore(): FallbackAttemptRequestStore {
    return MemoryFallbackAttemptRequestStore(
        mutableMapOf<String, FallbackAttemptRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAttemptRequestStore(
    var inner: FallbackAttemptRequestStore,
    var cache: TtlCache<FallbackAttemptRequest>,
    var hits: Int,
    var misses: Int,
) : FallbackAttemptRequestStore {
    override fun find(code: String): FallbackAttemptRequest? {
        val hit = (cache.get(code) as FallbackAttemptRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAttemptRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAttemptRequest> = inner.list()
}

fun newCachedFallbackAttemptRequestStore(inner: FallbackAttemptRequestStore, ttl: Int, clock: Clock): FallbackAttemptRequestStore {
    return CachedFallbackAttemptRequestStore(
        inner = inner,
        cache = newTtlCache<FallbackAttemptRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback attempt requests. Empty fields match all. */
data class FallbackAttemptRequestQuery(
    var segment: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAttemptRequest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAttemptRequest>): MutableList<FallbackAttemptRequest> {
        val matched = filter(items) { candidate: FallbackAttemptRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAttemptRequests(store: FallbackAttemptRequestStore, query: FallbackAttemptRequestQuery): MutableList<FallbackAttemptRequest> {
    return query.apply(store.list())
}
