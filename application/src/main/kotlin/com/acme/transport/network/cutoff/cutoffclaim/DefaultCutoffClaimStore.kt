package com.acme.transport.network.cutoff.cutoffclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default cutoff claims by code. */
interface DefaultCutoffClaimStore {
    fun find(code: String): DefaultCutoffClaim?
    fun save(item: DefaultCutoffClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCutoffClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCutoffClaimStore(
    var items: MutableMap<String, DefaultCutoffClaim>,
    var order: MutableList<String>,
) : DefaultCutoffClaimStore {
    override fun find(code: String): DefaultCutoffClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCutoffClaim) {
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

    override fun list(): MutableList<DefaultCutoffClaim> {
        val out = mutableListOf<DefaultCutoffClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCutoffClaimStore(): DefaultCutoffClaimStore {
    return MemoryDefaultCutoffClaimStore(mutableMapOf<String, DefaultCutoffClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCutoffClaimStore(
    var inner: DefaultCutoffClaimStore,
    var cache: TtlCache<DefaultCutoffClaim>,
    var hits: Int,
    var misses: Int,
) : DefaultCutoffClaimStore {
    override fun find(code: String): DefaultCutoffClaim? {
        val hit = (cache.get(code) as DefaultCutoffClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCutoffClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCutoffClaim> = inner.list()
}

fun newCachedDefaultCutoffClaimStore(inner: DefaultCutoffClaimStore, ttl: Int, clock: Clock): DefaultCutoffClaimStore {
    return CachedDefaultCutoffClaimStore(
        inner = inner,
        cache = newTtlCache<DefaultCutoffClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default cutoff claims. Empty fields match all. */
data class DefaultCutoffClaimQuery(
    var lane: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCutoffClaim): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCutoffClaim>): MutableList<DefaultCutoffClaim> {
        val matched = filter(items) { candidate: DefaultCutoffClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCutoffClaims(store: DefaultCutoffClaimStore, query: DefaultCutoffClaimQuery): MutableList<DefaultCutoffClaim> {
    return query.apply(store.list())
}
