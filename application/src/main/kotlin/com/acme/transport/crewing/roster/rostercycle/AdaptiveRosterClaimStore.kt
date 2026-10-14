package com.acme.transport.crewing.roster.rostercycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive roster claims by code. */
interface AdaptiveRosterClaimStore {
    fun find(code: String): AdaptiveRosterClaim?
    fun save(item: AdaptiveRosterClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveRosterClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveRosterClaimStore(
    var items: MutableMap<String, AdaptiveRosterClaim>,
    var order: MutableList<String>,
) : AdaptiveRosterClaimStore {
    override fun find(code: String): AdaptiveRosterClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveRosterClaim) {
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

    override fun list(): MutableList<AdaptiveRosterClaim> {
        val out = mutableListOf<AdaptiveRosterClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveRosterClaimStore(): AdaptiveRosterClaimStore {
    return MemoryAdaptiveRosterClaimStore(mutableMapOf<String, AdaptiveRosterClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveRosterClaimStore(
    var inner: AdaptiveRosterClaimStore,
    var cache: TtlCache<AdaptiveRosterClaim>,
    var hits: Int,
    var misses: Int,
) : AdaptiveRosterClaimStore {
    override fun find(code: String): AdaptiveRosterClaim? {
        val hit = (cache.get(code) as AdaptiveRosterClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveRosterClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveRosterClaim> = inner.list()
}

fun newCachedAdaptiveRosterClaimStore(inner: AdaptiveRosterClaimStore, ttl: Int, clock: Clock): AdaptiveRosterClaimStore {
    return CachedAdaptiveRosterClaimStore(
        inner = inner,
        cache = newTtlCache<AdaptiveRosterClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive roster claims. Empty fields match all. */
data class AdaptiveRosterClaimQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveRosterClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveRosterClaim>): MutableList<AdaptiveRosterClaim> {
        val matched = filter(items) { candidate: AdaptiveRosterClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveRosterClaims(store: AdaptiveRosterClaimStore, query: AdaptiveRosterClaimQuery): MutableList<AdaptiveRosterClaim> {
    return query.apply(store.list())
}
