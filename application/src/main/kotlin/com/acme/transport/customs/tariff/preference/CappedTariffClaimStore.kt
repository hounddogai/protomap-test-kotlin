package com.acme.transport.customs.tariff.preference

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped tariff claims by code. */
interface CappedTariffClaimStore {
    fun find(code: String): CappedTariffClaim?
    fun save(item: CappedTariffClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTariffClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTariffClaimStore(
    var items: MutableMap<String, CappedTariffClaim>,
    var order: MutableList<String>,
) : CappedTariffClaimStore {
    override fun find(code: String): CappedTariffClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTariffClaim) {
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

    override fun list(): MutableList<CappedTariffClaim> {
        val out = mutableListOf<CappedTariffClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTariffClaimStore(): CappedTariffClaimStore {
    return MemoryCappedTariffClaimStore(mutableMapOf<String, CappedTariffClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTariffClaimStore(
    var inner: CappedTariffClaimStore,
    var cache: TtlCache<CappedTariffClaim>,
    var hits: Int,
    var misses: Int,
) : CappedTariffClaimStore {
    override fun find(code: String): CappedTariffClaim? {
        val hit = (cache.get(code) as CappedTariffClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTariffClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTariffClaim> = inner.list()
}

fun newCachedCappedTariffClaimStore(inner: CappedTariffClaimStore, ttl: Int, clock: Clock): CappedTariffClaimStore {
    return CachedCappedTariffClaimStore(
        inner = inner,
        cache = newTtlCache<CappedTariffClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped tariff claims. Empty fields match all. */
data class CappedTariffClaimQuery(
    var country: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTariffClaim): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTariffClaim>): MutableList<CappedTariffClaim> {
        val matched = filter(items) { candidate: CappedTariffClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTariffClaims(store: CappedTariffClaimStore, query: CappedTariffClaimQuery): MutableList<CappedTariffClaim> {
    return query.apply(store.list())
}
