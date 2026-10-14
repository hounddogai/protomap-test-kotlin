package com.acme.transport.customs.tariff.policy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient tariff claims by code. */
interface LenientTariffClaimStore {
    fun find(code: String): LenientTariffClaim?
    fun save(item: LenientTariffClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientTariffClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientTariffClaimStore(
    var items: MutableMap<String, LenientTariffClaim>,
    var order: MutableList<String>,
) : LenientTariffClaimStore {
    override fun find(code: String): LenientTariffClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientTariffClaim) {
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

    override fun list(): MutableList<LenientTariffClaim> {
        val out = mutableListOf<LenientTariffClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientTariffClaimStore(): LenientTariffClaimStore {
    return MemoryLenientTariffClaimStore(mutableMapOf<String, LenientTariffClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientTariffClaimStore(
    var inner: LenientTariffClaimStore,
    var cache: TtlCache<LenientTariffClaim>,
    var hits: Int,
    var misses: Int,
) : LenientTariffClaimStore {
    override fun find(code: String): LenientTariffClaim? {
        val hit = (cache.get(code) as LenientTariffClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientTariffClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientTariffClaim> = inner.list()
}

fun newCachedLenientTariffClaimStore(inner: LenientTariffClaimStore, ttl: Int, clock: Clock): LenientTariffClaimStore {
    return CachedLenientTariffClaimStore(
        inner = inner,
        cache = newTtlCache<LenientTariffClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient tariff claims. Empty fields match all. */
data class LenientTariffClaimQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientTariffClaim): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientTariffClaim>): MutableList<LenientTariffClaim> {
        val matched = filter(items) { candidate: LenientTariffClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientTariffClaims(store: LenientTariffClaimStore, query: LenientTariffClaimQuery): MutableList<LenientTariffClaim> {
    return query.apply(store.list())
}
