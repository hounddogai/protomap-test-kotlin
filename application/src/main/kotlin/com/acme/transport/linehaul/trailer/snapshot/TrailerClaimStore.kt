package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer claims by code. */
interface TrailerClaimStore {
    fun find(code: String): TrailerClaim?
    fun save(item: TrailerClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerClaimStore(
    var items: MutableMap<String, TrailerClaim>,
    var order: MutableList<String>,
) : TrailerClaimStore {
    override fun find(code: String): TrailerClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerClaim) {
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

    override fun list(): MutableList<TrailerClaim> {
        val out = mutableListOf<TrailerClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerClaimStore(): TrailerClaimStore {
    return MemoryTrailerClaimStore(mutableMapOf<String, TrailerClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerClaimStore(
    var inner: TrailerClaimStore,
    var cache: TtlCache<TrailerClaim>,
    var hits: Int,
    var misses: Int,
) : TrailerClaimStore {
    override fun find(code: String): TrailerClaim? {
        val hit = (cache.get(code) as TrailerClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerClaim> = inner.list()
}

fun newCachedTrailerClaimStore(inner: TrailerClaimStore, ttl: Int, clock: Clock): TrailerClaimStore {
    return CachedTrailerClaimStore(
        inner = inner,
        cache = newTtlCache<TrailerClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer claims. Empty fields match all. */
data class TrailerClaimQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<TrailerClaim>): MutableList<TrailerClaim> {
        val matched = filter(items) { candidate: TrailerClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerClaims(store: TrailerClaimStore, query: TrailerClaimQuery): MutableList<TrailerClaim> {
    return query.apply(store.list())
}
