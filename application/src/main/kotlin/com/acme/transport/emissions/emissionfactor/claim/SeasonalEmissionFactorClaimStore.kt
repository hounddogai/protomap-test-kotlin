package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal emission factor claims by code. */
interface SeasonalEmissionFactorClaimStore {
    fun find(code: String): SeasonalEmissionFactorClaim?
    fun save(item: SeasonalEmissionFactorClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalEmissionFactorClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalEmissionFactorClaimStore(
    var items: MutableMap<String, SeasonalEmissionFactorClaim>,
    var order: MutableList<String>,
) : SeasonalEmissionFactorClaimStore {
    override fun find(code: String): SeasonalEmissionFactorClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalEmissionFactorClaim) {
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

    override fun list(): MutableList<SeasonalEmissionFactorClaim> {
        val out = mutableListOf<SeasonalEmissionFactorClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalEmissionFactorClaimStore(): SeasonalEmissionFactorClaimStore {
    return MemorySeasonalEmissionFactorClaimStore(
        mutableMapOf<String, SeasonalEmissionFactorClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalEmissionFactorClaimStore(
    var inner: SeasonalEmissionFactorClaimStore,
    var cache: TtlCache<SeasonalEmissionFactorClaim>,
    var hits: Int,
    var misses: Int,
) : SeasonalEmissionFactorClaimStore {
    override fun find(code: String): SeasonalEmissionFactorClaim? {
        val hit = (cache.get(code) as SeasonalEmissionFactorClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalEmissionFactorClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalEmissionFactorClaim> = inner.list()
}

fun newCachedSeasonalEmissionFactorClaimStore(inner: SeasonalEmissionFactorClaimStore, ttl: Int, clock: Clock): SeasonalEmissionFactorClaimStore {
    return CachedSeasonalEmissionFactorClaimStore(
        inner = inner,
        cache = newTtlCache<SeasonalEmissionFactorClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal emission factor claims. Empty fields match all. */
data class SeasonalEmissionFactorClaimQuery(
    var source: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalEmissionFactorClaim): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<SeasonalEmissionFactorClaim>): MutableList<SeasonalEmissionFactorClaim> {
        val matched = filter(items) { candidate: SeasonalEmissionFactorClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalEmissionFactorClaims(store: SeasonalEmissionFactorClaimStore, query: SeasonalEmissionFactorClaimQuery): MutableList<SeasonalEmissionFactorClaim> {
    return query.apply(store.list())
}
