package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor claims by code. */
interface EmissionFactorClaimStore {
    fun find(code: String): EmissionFactorClaim?
    fun save(item: EmissionFactorClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorClaimStore(
    var items: MutableMap<String, EmissionFactorClaim>,
    var order: MutableList<String>,
) : EmissionFactorClaimStore {
    override fun find(code: String): EmissionFactorClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorClaim) {
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

    override fun list(): MutableList<EmissionFactorClaim> {
        val out = mutableListOf<EmissionFactorClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorClaimStore(): EmissionFactorClaimStore {
    return MemoryEmissionFactorClaimStore(mutableMapOf<String, EmissionFactorClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorClaimStore(
    var inner: EmissionFactorClaimStore,
    var cache: TtlCache<EmissionFactorClaim>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorClaimStore {
    override fun find(code: String): EmissionFactorClaim? {
        val hit = (cache.get(code) as EmissionFactorClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorClaim> = inner.list()
}

fun newCachedEmissionFactorClaimStore(inner: EmissionFactorClaimStore, ttl: Int, clock: Clock): EmissionFactorClaimStore {
    return CachedEmissionFactorClaimStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor claims. Empty fields match all. */
data class EmissionFactorClaimQuery(
    var region: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorClaim): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<EmissionFactorClaim>): MutableList<EmissionFactorClaim> {
        val matched = filter(items) { candidate: EmissionFactorClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorClaims(store: EmissionFactorClaimStore, query: EmissionFactorClaimQuery): MutableList<EmissionFactorClaim> {
    return query.apply(store.list())
}
