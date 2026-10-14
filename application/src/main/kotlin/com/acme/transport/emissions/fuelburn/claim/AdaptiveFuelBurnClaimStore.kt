package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive fuel burn claims by code. */
interface AdaptiveFuelBurnClaimStore {
    fun find(code: String): AdaptiveFuelBurnClaim?
    fun save(item: AdaptiveFuelBurnClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveFuelBurnClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveFuelBurnClaimStore(
    var items: MutableMap<String, AdaptiveFuelBurnClaim>,
    var order: MutableList<String>,
) : AdaptiveFuelBurnClaimStore {
    override fun find(code: String): AdaptiveFuelBurnClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveFuelBurnClaim) {
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

    override fun list(): MutableList<AdaptiveFuelBurnClaim> {
        val out = mutableListOf<AdaptiveFuelBurnClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveFuelBurnClaimStore(): AdaptiveFuelBurnClaimStore {
    return MemoryAdaptiveFuelBurnClaimStore(
        mutableMapOf<String, AdaptiveFuelBurnClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveFuelBurnClaimStore(
    var inner: AdaptiveFuelBurnClaimStore,
    var cache: TtlCache<AdaptiveFuelBurnClaim>,
    var hits: Int,
    var misses: Int,
) : AdaptiveFuelBurnClaimStore {
    override fun find(code: String): AdaptiveFuelBurnClaim? {
        val hit = (cache.get(code) as AdaptiveFuelBurnClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveFuelBurnClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveFuelBurnClaim> = inner.list()
}

fun newCachedAdaptiveFuelBurnClaimStore(inner: AdaptiveFuelBurnClaimStore, ttl: Int, clock: Clock): AdaptiveFuelBurnClaimStore {
    return CachedAdaptiveFuelBurnClaimStore(
        inner = inner,
        cache = newTtlCache<AdaptiveFuelBurnClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive fuel burn claims. Empty fields match all. */
data class AdaptiveFuelBurnClaimQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveFuelBurnClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveFuelBurnClaim>): MutableList<AdaptiveFuelBurnClaim> {
        val matched = filter(items) { candidate: AdaptiveFuelBurnClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveFuelBurnClaims(store: AdaptiveFuelBurnClaimStore, query: AdaptiveFuelBurnClaimQuery): MutableList<AdaptiveFuelBurnClaim> {
    return query.apply(store.list())
}
