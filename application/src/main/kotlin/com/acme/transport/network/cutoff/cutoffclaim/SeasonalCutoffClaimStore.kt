package com.acme.transport.network.cutoff.cutoffclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal cutoff claims by code. */
interface SeasonalCutoffClaimStore {
    fun find(code: String): SeasonalCutoffClaim?
    fun save(item: SeasonalCutoffClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCutoffClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCutoffClaimStore(
    var items: MutableMap<String, SeasonalCutoffClaim>,
    var order: MutableList<String>,
) : SeasonalCutoffClaimStore {
    override fun find(code: String): SeasonalCutoffClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCutoffClaim) {
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

    override fun list(): MutableList<SeasonalCutoffClaim> {
        val out = mutableListOf<SeasonalCutoffClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCutoffClaimStore(): SeasonalCutoffClaimStore {
    return MemorySeasonalCutoffClaimStore(mutableMapOf<String, SeasonalCutoffClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCutoffClaimStore(
    var inner: SeasonalCutoffClaimStore,
    var cache: TtlCache<SeasonalCutoffClaim>,
    var hits: Int,
    var misses: Int,
) : SeasonalCutoffClaimStore {
    override fun find(code: String): SeasonalCutoffClaim? {
        val hit = (cache.get(code) as SeasonalCutoffClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCutoffClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCutoffClaim> = inner.list()
}

fun newCachedSeasonalCutoffClaimStore(inner: SeasonalCutoffClaimStore, ttl: Int, clock: Clock): SeasonalCutoffClaimStore {
    return CachedSeasonalCutoffClaimStore(
        inner = inner,
        cache = newTtlCache<SeasonalCutoffClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal cutoff claims. Empty fields match all. */
data class SeasonalCutoffClaimQuery(
    var category: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCutoffClaim): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalCutoffClaim>): MutableList<SeasonalCutoffClaim> {
        val matched = filter(items) { candidate: SeasonalCutoffClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCutoffClaims(store: SeasonalCutoffClaimStore, query: SeasonalCutoffClaimQuery): MutableList<SeasonalCutoffClaim> {
    return query.apply(store.list())
}
