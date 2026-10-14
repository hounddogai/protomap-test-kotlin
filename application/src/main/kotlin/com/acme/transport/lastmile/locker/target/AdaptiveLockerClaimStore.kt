package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive locker claims by code. */
interface AdaptiveLockerClaimStore {
    fun find(code: String): AdaptiveLockerClaim?
    fun save(item: AdaptiveLockerClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLockerClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLockerClaimStore(
    var items: MutableMap<String, AdaptiveLockerClaim>,
    var order: MutableList<String>,
) : AdaptiveLockerClaimStore {
    override fun find(code: String): AdaptiveLockerClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLockerClaim) {
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

    override fun list(): MutableList<AdaptiveLockerClaim> {
        val out = mutableListOf<AdaptiveLockerClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLockerClaimStore(): AdaptiveLockerClaimStore {
    return MemoryAdaptiveLockerClaimStore(mutableMapOf<String, AdaptiveLockerClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLockerClaimStore(
    var inner: AdaptiveLockerClaimStore,
    var cache: TtlCache<AdaptiveLockerClaim>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLockerClaimStore {
    override fun find(code: String): AdaptiveLockerClaim? {
        val hit = (cache.get(code) as AdaptiveLockerClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLockerClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLockerClaim> = inner.list()
}

fun newCachedAdaptiveLockerClaimStore(inner: AdaptiveLockerClaimStore, ttl: Int, clock: Clock): AdaptiveLockerClaimStore {
    return CachedAdaptiveLockerClaimStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLockerClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive locker claims. Empty fields match all. */
data class AdaptiveLockerClaimQuery(
    var depot: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLockerClaim): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<AdaptiveLockerClaim>): MutableList<AdaptiveLockerClaim> {
        val matched = filter(items) { candidate: AdaptiveLockerClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLockerClaims(store: AdaptiveLockerClaimStore, query: AdaptiveLockerClaimQuery): MutableList<AdaptiveLockerClaim> {
    return query.apply(store.list())
}
