package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard claims by code. */
interface TrailerYardClaimStore {
    fun find(code: String): TrailerYardClaim?
    fun save(item: TrailerYardClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardClaimStore(
    var items: MutableMap<String, TrailerYardClaim>,
    var order: MutableList<String>,
) : TrailerYardClaimStore {
    override fun find(code: String): TrailerYardClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardClaim) {
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

    override fun list(): MutableList<TrailerYardClaim> {
        val out = mutableListOf<TrailerYardClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardClaimStore(): TrailerYardClaimStore {
    return MemoryTrailerYardClaimStore(mutableMapOf<String, TrailerYardClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardClaimStore(
    var inner: TrailerYardClaimStore,
    var cache: TtlCache<TrailerYardClaim>,
    var hits: Int,
    var misses: Int,
) : TrailerYardClaimStore {
    override fun find(code: String): TrailerYardClaim? {
        val hit = (cache.get(code) as TrailerYardClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardClaim> = inner.list()
}

fun newCachedTrailerYardClaimStore(inner: TrailerYardClaimStore, ttl: Int, clock: Clock): TrailerYardClaimStore {
    return CachedTrailerYardClaimStore(
        inner = inner,
        cache = newTtlCache<TrailerYardClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard claims. Empty fields match all. */
data class TrailerYardClaimQuery(
    var lane: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardClaim): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardClaim>): MutableList<TrailerYardClaim> {
        val matched = filter(items) { candidate: TrailerYardClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardClaims(store: TrailerYardClaimStore, query: TrailerYardClaimQuery): MutableList<TrailerYardClaim> {
    return query.apply(store.list())
}
