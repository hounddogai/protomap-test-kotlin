package com.acme.transport.crewing.swap.claim

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap claims by code. */
interface SwapClaimStore {
    fun find(code: String): SwapClaim?
    fun save(item: SwapClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapClaimStore(
    var items: MutableMap<String, SwapClaim>,
    var order: MutableList<String>,
) : SwapClaimStore {
    override fun find(code: String): SwapClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapClaim) {
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

    override fun list(): MutableList<SwapClaim> {
        val out = mutableListOf<SwapClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapClaimStore(): SwapClaimStore {
    return MemorySwapClaimStore(mutableMapOf<String, SwapClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapClaimStore(
    var inner: SwapClaimStore,
    var cache: TtlCache<SwapClaim>,
    var hits: Int,
    var misses: Int,
) : SwapClaimStore {
    override fun find(code: String): SwapClaim? {
        val hit = (cache.get(code) as SwapClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapClaim> = inner.list()
}

fun newCachedSwapClaimStore(inner: SwapClaimStore, ttl: Int, clock: Clock): SwapClaimStore {
    return CachedSwapClaimStore(
        inner = inner,
        cache = newTtlCache<SwapClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap claims. Empty fields match all. */
data class SwapClaimQuery(
    var segment: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapClaim): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapClaim>): MutableList<SwapClaim> {
        val matched = filter(items) { candidate: SwapClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapClaims(store: SwapClaimStore, query: SwapClaimQuery): MutableList<SwapClaim> {
    return query.apply(store.list())
}
