package com.acme.transport.crewing.restbreak.preference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break claims by code. */
interface RestBreakClaimStore {
    fun find(code: String): RestBreakClaim?
    fun save(item: RestBreakClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakClaimStore(
    var items: MutableMap<String, RestBreakClaim>,
    var order: MutableList<String>,
) : RestBreakClaimStore {
    override fun find(code: String): RestBreakClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakClaim) {
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

    override fun list(): MutableList<RestBreakClaim> {
        val out = mutableListOf<RestBreakClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakClaimStore(): RestBreakClaimStore {
    return MemoryRestBreakClaimStore(mutableMapOf<String, RestBreakClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakClaimStore(
    var inner: RestBreakClaimStore,
    var cache: TtlCache<RestBreakClaim>,
    var hits: Int,
    var misses: Int,
) : RestBreakClaimStore {
    override fun find(code: String): RestBreakClaim? {
        val hit = (cache.get(code) as RestBreakClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakClaim> = inner.list()
}

fun newCachedRestBreakClaimStore(inner: RestBreakClaimStore, ttl: Int, clock: Clock): RestBreakClaimStore {
    return CachedRestBreakClaimStore(
        inner = inner,
        cache = newTtlCache<RestBreakClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break claims. Empty fields match all. */
data class RestBreakClaimQuery(
    var channel: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakClaim): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakClaim>): MutableList<RestBreakClaim> {
        val matched = filter(items) { candidate: RestBreakClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakClaims(store: RestBreakClaimStore, query: RestBreakClaimQuery): MutableList<RestBreakClaim> {
    return query.apply(store.list())
}
