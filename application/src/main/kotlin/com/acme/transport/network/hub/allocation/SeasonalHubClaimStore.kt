package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal hub claims by code. */
interface SeasonalHubClaimStore {
    fun find(code: String): SeasonalHubClaim?
    fun save(item: SeasonalHubClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalHubClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalHubClaimStore(
    var items: MutableMap<String, SeasonalHubClaim>,
    var order: MutableList<String>,
) : SeasonalHubClaimStore {
    override fun find(code: String): SeasonalHubClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalHubClaim) {
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

    override fun list(): MutableList<SeasonalHubClaim> {
        val out = mutableListOf<SeasonalHubClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalHubClaimStore(): SeasonalHubClaimStore {
    return MemorySeasonalHubClaimStore(mutableMapOf<String, SeasonalHubClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalHubClaimStore(
    var inner: SeasonalHubClaimStore,
    var cache: TtlCache<SeasonalHubClaim>,
    var hits: Int,
    var misses: Int,
) : SeasonalHubClaimStore {
    override fun find(code: String): SeasonalHubClaim? {
        val hit = (cache.get(code) as SeasonalHubClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalHubClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalHubClaim> = inner.list()
}

fun newCachedSeasonalHubClaimStore(inner: SeasonalHubClaimStore, ttl: Int, clock: Clock): SeasonalHubClaimStore {
    return CachedSeasonalHubClaimStore(
        inner = inner,
        cache = newTtlCache<SeasonalHubClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal hub claims. Empty fields match all. */
data class SeasonalHubClaimQuery(
    var channel: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalHubClaim): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalHubClaim>): MutableList<SeasonalHubClaim> {
        val matched = filter(items) { candidate: SeasonalHubClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalHubClaims(store: SeasonalHubClaimStore, query: SeasonalHubClaimQuery): MutableList<SeasonalHubClaim> {
    return query.apply(store.list())
}
