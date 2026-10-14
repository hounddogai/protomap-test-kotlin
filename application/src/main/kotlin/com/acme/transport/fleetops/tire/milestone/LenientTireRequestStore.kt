package com.acme.transport.fleetops.tire.milestone

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient tire requests by code. */
interface LenientTireRequestStore {
    fun find(code: String): LenientTireRequest?
    fun save(item: LenientTireRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientTireRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientTireRequestStore(
    var items: MutableMap<String, LenientTireRequest>,
    var order: MutableList<String>,
) : LenientTireRequestStore {
    override fun find(code: String): LenientTireRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientTireRequest) {
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

    override fun list(): MutableList<LenientTireRequest> {
        val out = mutableListOf<LenientTireRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientTireRequestStore(): LenientTireRequestStore {
    return MemoryLenientTireRequestStore(mutableMapOf<String, LenientTireRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientTireRequestStore(
    var inner: LenientTireRequestStore,
    var cache: TtlCache<LenientTireRequest>,
    var hits: Int,
    var misses: Int,
) : LenientTireRequestStore {
    override fun find(code: String): LenientTireRequest? {
        val hit = (cache.get(code) as LenientTireRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientTireRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientTireRequest> = inner.list()
}

fun newCachedLenientTireRequestStore(inner: LenientTireRequestStore, ttl: Int, clock: Clock): LenientTireRequestStore {
    return CachedLenientTireRequestStore(
        inner = inner,
        cache = newTtlCache<LenientTireRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient tire requests. Empty fields match all. */
data class LenientTireRequestQuery(
    var country: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientTireRequest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientTireRequest>): MutableList<LenientTireRequest> {
        val matched = filter(items) { candidate: LenientTireRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientTireRequests(store: LenientTireRequestStore, query: LenientTireRequestQuery): MutableList<LenientTireRequest> {
    return query.apply(store.list())
}
