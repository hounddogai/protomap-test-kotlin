package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional emission factor claims by code. */
interface RegionalEmissionFactorClaimStore {
    fun find(code: String): RegionalEmissionFactorClaim?
    fun save(item: RegionalEmissionFactorClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalEmissionFactorClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalEmissionFactorClaimStore(
    var items: MutableMap<String, RegionalEmissionFactorClaim>,
    var order: MutableList<String>,
) : RegionalEmissionFactorClaimStore {
    override fun find(code: String): RegionalEmissionFactorClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalEmissionFactorClaim) {
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

    override fun list(): MutableList<RegionalEmissionFactorClaim> {
        val out = mutableListOf<RegionalEmissionFactorClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalEmissionFactorClaimStore(): RegionalEmissionFactorClaimStore {
    return MemoryRegionalEmissionFactorClaimStore(
        mutableMapOf<String, RegionalEmissionFactorClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalEmissionFactorClaimStore(
    var inner: RegionalEmissionFactorClaimStore,
    var cache: TtlCache<RegionalEmissionFactorClaim>,
    var hits: Int,
    var misses: Int,
) : RegionalEmissionFactorClaimStore {
    override fun find(code: String): RegionalEmissionFactorClaim? {
        val hit = (cache.get(code) as RegionalEmissionFactorClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalEmissionFactorClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalEmissionFactorClaim> = inner.list()
}

fun newCachedRegionalEmissionFactorClaimStore(inner: RegionalEmissionFactorClaimStore, ttl: Int, clock: Clock): RegionalEmissionFactorClaimStore {
    return CachedRegionalEmissionFactorClaimStore(
        inner = inner,
        cache = newTtlCache<RegionalEmissionFactorClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional emission factor claims. Empty fields match all. */
data class RegionalEmissionFactorClaimQuery(
    var locale: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalEmissionFactorClaim): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalEmissionFactorClaim>): MutableList<RegionalEmissionFactorClaim> {
        val matched = filter(items) { candidate: RegionalEmissionFactorClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalEmissionFactorClaims(store: RegionalEmissionFactorClaimStore, query: RegionalEmissionFactorClaimQuery): MutableList<RegionalEmissionFactorClaim> {
    return query.apply(store.list())
}
