package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary trailer claims by code. */
interface PrimaryTrailerClaimStore {
    fun find(code: String): PrimaryTrailerClaim?
    fun save(item: PrimaryTrailerClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTrailerClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTrailerClaimStore(
    var items: MutableMap<String, PrimaryTrailerClaim>,
    var order: MutableList<String>,
) : PrimaryTrailerClaimStore {
    override fun find(code: String): PrimaryTrailerClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTrailerClaim) {
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

    override fun list(): MutableList<PrimaryTrailerClaim> {
        val out = mutableListOf<PrimaryTrailerClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTrailerClaimStore(): PrimaryTrailerClaimStore {
    return MemoryPrimaryTrailerClaimStore(mutableMapOf<String, PrimaryTrailerClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTrailerClaimStore(
    var inner: PrimaryTrailerClaimStore,
    var cache: TtlCache<PrimaryTrailerClaim>,
    var hits: Int,
    var misses: Int,
) : PrimaryTrailerClaimStore {
    override fun find(code: String): PrimaryTrailerClaim? {
        val hit = (cache.get(code) as PrimaryTrailerClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTrailerClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTrailerClaim> = inner.list()
}

fun newCachedPrimaryTrailerClaimStore(inner: PrimaryTrailerClaimStore, ttl: Int, clock: Clock): PrimaryTrailerClaimStore {
    return CachedPrimaryTrailerClaimStore(
        inner = inner,
        cache = newTtlCache<PrimaryTrailerClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary trailer claims. Empty fields match all. */
data class PrimaryTrailerClaimQuery(
    var segment: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTrailerClaim): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTrailerClaim>): MutableList<PrimaryTrailerClaim> {
        val matched = filter(items) { candidate: PrimaryTrailerClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTrailerClaims(store: PrimaryTrailerClaimStore, query: PrimaryTrailerClaimQuery): MutableList<PrimaryTrailerClaim> {
    return query.apply(store.list())
}
