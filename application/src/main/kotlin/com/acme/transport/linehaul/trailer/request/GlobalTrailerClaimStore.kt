package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global trailer claims by code. */
interface GlobalTrailerClaimStore {
    fun find(code: String): GlobalTrailerClaim?
    fun save(item: GlobalTrailerClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTrailerClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTrailerClaimStore(
    var items: MutableMap<String, GlobalTrailerClaim>,
    var order: MutableList<String>,
) : GlobalTrailerClaimStore {
    override fun find(code: String): GlobalTrailerClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTrailerClaim) {
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

    override fun list(): MutableList<GlobalTrailerClaim> {
        val out = mutableListOf<GlobalTrailerClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTrailerClaimStore(): GlobalTrailerClaimStore {
    return MemoryGlobalTrailerClaimStore(mutableMapOf<String, GlobalTrailerClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTrailerClaimStore(
    var inner: GlobalTrailerClaimStore,
    var cache: TtlCache<GlobalTrailerClaim>,
    var hits: Int,
    var misses: Int,
) : GlobalTrailerClaimStore {
    override fun find(code: String): GlobalTrailerClaim? {
        val hit = (cache.get(code) as GlobalTrailerClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTrailerClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTrailerClaim> = inner.list()
}

fun newCachedGlobalTrailerClaimStore(inner: GlobalTrailerClaimStore, ttl: Int, clock: Clock): GlobalTrailerClaimStore {
    return CachedGlobalTrailerClaimStore(
        inner = inner,
        cache = newTtlCache<GlobalTrailerClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global trailer claims. Empty fields match all. */
data class GlobalTrailerClaimQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTrailerClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<GlobalTrailerClaim>): MutableList<GlobalTrailerClaim> {
        val matched = filter(items) { candidate: GlobalTrailerClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTrailerClaims(store: GlobalTrailerClaimStore, query: GlobalTrailerClaimQuery): MutableList<GlobalTrailerClaim> {
    return query.apply(store.list())
}
