package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default trailer claims by code. */
interface DefaultTrailerClaimStore {
    fun find(code: String): DefaultTrailerClaim?
    fun save(item: DefaultTrailerClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTrailerClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTrailerClaimStore(
    var items: MutableMap<String, DefaultTrailerClaim>,
    var order: MutableList<String>,
) : DefaultTrailerClaimStore {
    override fun find(code: String): DefaultTrailerClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTrailerClaim) {
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

    override fun list(): MutableList<DefaultTrailerClaim> {
        val out = mutableListOf<DefaultTrailerClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTrailerClaimStore(): DefaultTrailerClaimStore {
    return MemoryDefaultTrailerClaimStore(mutableMapOf<String, DefaultTrailerClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTrailerClaimStore(
    var inner: DefaultTrailerClaimStore,
    var cache: TtlCache<DefaultTrailerClaim>,
    var hits: Int,
    var misses: Int,
) : DefaultTrailerClaimStore {
    override fun find(code: String): DefaultTrailerClaim? {
        val hit = (cache.get(code) as DefaultTrailerClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTrailerClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTrailerClaim> = inner.list()
}

fun newCachedDefaultTrailerClaimStore(inner: DefaultTrailerClaimStore, ttl: Int, clock: Clock): DefaultTrailerClaimStore {
    return CachedDefaultTrailerClaimStore(
        inner = inner,
        cache = newTtlCache<DefaultTrailerClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default trailer claims. Empty fields match all. */
data class DefaultTrailerClaimQuery(
    var depot: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTrailerClaim): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTrailerClaim>): MutableList<DefaultTrailerClaim> {
        val matched = filter(items) { candidate: DefaultTrailerClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTrailerClaims(store: DefaultTrailerClaimStore, query: DefaultTrailerClaimQuery): MutableList<DefaultTrailerClaim> {
    return query.apply(store.list())
}
