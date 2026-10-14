package com.acme.transport.network.servicearea.weightedareaclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps adaptive service area claims by code. */
interface AdaptiveServiceAreaClaimStore {
    fun find(code: String): AdaptiveServiceAreaClaim?
    fun save(item: AdaptiveServiceAreaClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveServiceAreaClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveServiceAreaClaimStore(
    var items: MutableMap<String, AdaptiveServiceAreaClaim>,
    var order: MutableList<String>,
) : AdaptiveServiceAreaClaimStore {
    override fun find(code: String): AdaptiveServiceAreaClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveServiceAreaClaim) {
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

    override fun list(): MutableList<AdaptiveServiceAreaClaim> {
        val out = mutableListOf<AdaptiveServiceAreaClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveServiceAreaClaimStore(): AdaptiveServiceAreaClaimStore {
    return MemoryAdaptiveServiceAreaClaimStore(
        mutableMapOf<String, AdaptiveServiceAreaClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveServiceAreaClaimStore(
    var inner: AdaptiveServiceAreaClaimStore,
    var cache: TtlCache<AdaptiveServiceAreaClaim>,
    var hits: Int,
    var misses: Int,
) : AdaptiveServiceAreaClaimStore {
    override fun find(code: String): AdaptiveServiceAreaClaim? {
        val hit = (cache.get(code) as AdaptiveServiceAreaClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveServiceAreaClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveServiceAreaClaim> = inner.list()
}

fun newCachedAdaptiveServiceAreaClaimStore(inner: AdaptiveServiceAreaClaimStore, ttl: Int, clock: Clock): AdaptiveServiceAreaClaimStore {
    return CachedAdaptiveServiceAreaClaimStore(
        inner = inner,
        cache = newTtlCache<AdaptiveServiceAreaClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive service area claims. Empty fields match all. */
data class AdaptiveServiceAreaClaimQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveServiceAreaClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveServiceAreaClaim>): MutableList<AdaptiveServiceAreaClaim> {
        val matched = filter(items) { candidate: AdaptiveServiceAreaClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveServiceAreaClaims(store: AdaptiveServiceAreaClaimStore, query: AdaptiveServiceAreaClaimQuery): MutableList<AdaptiveServiceAreaClaim> {
    return query.apply(store.list())
}
