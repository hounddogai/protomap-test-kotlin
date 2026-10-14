package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive near miss claims by code. */
interface AdaptiveNearMissClaimStore {
    fun find(code: String): AdaptiveNearMissClaim?
    fun save(item: AdaptiveNearMissClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveNearMissClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveNearMissClaimStore(
    var items: MutableMap<String, AdaptiveNearMissClaim>,
    var order: MutableList<String>,
) : AdaptiveNearMissClaimStore {
    override fun find(code: String): AdaptiveNearMissClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveNearMissClaim) {
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

    override fun list(): MutableList<AdaptiveNearMissClaim> {
        val out = mutableListOf<AdaptiveNearMissClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveNearMissClaimStore(): AdaptiveNearMissClaimStore {
    return MemoryAdaptiveNearMissClaimStore(
        mutableMapOf<String, AdaptiveNearMissClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveNearMissClaimStore(
    var inner: AdaptiveNearMissClaimStore,
    var cache: TtlCache<AdaptiveNearMissClaim>,
    var hits: Int,
    var misses: Int,
) : AdaptiveNearMissClaimStore {
    override fun find(code: String): AdaptiveNearMissClaim? {
        val hit = (cache.get(code) as AdaptiveNearMissClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveNearMissClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveNearMissClaim> = inner.list()
}

fun newCachedAdaptiveNearMissClaimStore(inner: AdaptiveNearMissClaimStore, ttl: Int, clock: Clock): AdaptiveNearMissClaimStore {
    return CachedAdaptiveNearMissClaimStore(
        inner = inner,
        cache = newTtlCache<AdaptiveNearMissClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive near miss claims. Empty fields match all. */
data class AdaptiveNearMissClaimQuery(
    var channel: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveNearMissClaim): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveNearMissClaim>): MutableList<AdaptiveNearMissClaim> {
        val matched = filter(items) { candidate: AdaptiveNearMissClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveNearMissClaims(store: AdaptiveNearMissClaimStore, query: AdaptiveNearMissClaimQuery): MutableList<AdaptiveNearMissClaim> {
    return query.apply(store.list())
}
