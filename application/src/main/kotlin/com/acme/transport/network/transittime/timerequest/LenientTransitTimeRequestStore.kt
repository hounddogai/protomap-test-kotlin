package com.acme.transport.network.transittime.timerequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient transit time requests by code. */
interface LenientTransitTimeRequestStore {
    fun find(code: String): LenientTransitTimeRequest?
    fun save(item: LenientTransitTimeRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientTransitTimeRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientTransitTimeRequestStore(
    var items: MutableMap<String, LenientTransitTimeRequest>,
    var order: MutableList<String>,
) : LenientTransitTimeRequestStore {
    override fun find(code: String): LenientTransitTimeRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientTransitTimeRequest) {
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

    override fun list(): MutableList<LenientTransitTimeRequest> {
        val out = mutableListOf<LenientTransitTimeRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientTransitTimeRequestStore(): LenientTransitTimeRequestStore {
    return MemoryLenientTransitTimeRequestStore(
        mutableMapOf<String, LenientTransitTimeRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientTransitTimeRequestStore(
    var inner: LenientTransitTimeRequestStore,
    var cache: TtlCache<LenientTransitTimeRequest>,
    var hits: Int,
    var misses: Int,
) : LenientTransitTimeRequestStore {
    override fun find(code: String): LenientTransitTimeRequest? {
        val hit = (cache.get(code) as LenientTransitTimeRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientTransitTimeRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientTransitTimeRequest> = inner.list()
}

fun newCachedLenientTransitTimeRequestStore(inner: LenientTransitTimeRequestStore, ttl: Int, clock: Clock): LenientTransitTimeRequestStore {
    return CachedLenientTransitTimeRequestStore(
        inner = inner,
        cache = newTtlCache<LenientTransitTimeRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient transit time requests. Empty fields match all. */
data class LenientTransitTimeRequestQuery(
    var category: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientTransitTimeRequest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientTransitTimeRequest>): MutableList<LenientTransitTimeRequest> {
        val matched = filter(items) { candidate: LenientTransitTimeRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientTransitTimeRequests(store: LenientTransitTimeRequestStore, query: LenientTransitTimeRequestQuery): MutableList<LenientTransitTimeRequest> {
    return query.apply(store.list())
}
