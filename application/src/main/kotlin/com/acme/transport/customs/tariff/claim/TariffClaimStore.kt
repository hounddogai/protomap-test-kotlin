package com.acme.transport.customs.tariff.claim

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff claims by code. */
interface TariffClaimStore {
    fun find(code: String): TariffClaim?
    fun save(item: TariffClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffClaimStore(
    var items: MutableMap<String, TariffClaim>,
    var order: MutableList<String>,
) : TariffClaimStore {
    override fun find(code: String): TariffClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffClaim) {
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

    override fun list(): MutableList<TariffClaim> {
        val out = mutableListOf<TariffClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffClaimStore(): TariffClaimStore {
    return MemoryTariffClaimStore(mutableMapOf<String, TariffClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffClaimStore(
    var inner: TariffClaimStore,
    var cache: TtlCache<TariffClaim>,
    var hits: Int,
    var misses: Int,
) : TariffClaimStore {
    override fun find(code: String): TariffClaim? {
        val hit = (cache.get(code) as TariffClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffClaim> = inner.list()
}

fun newCachedTariffClaimStore(inner: TariffClaimStore, ttl: Int, clock: Clock): TariffClaimStore {
    return CachedTariffClaimStore(
        inner = inner,
        cache = newTtlCache<TariffClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff claims. Empty fields match all. */
data class TariffClaimQuery(
    var label: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffClaim>): MutableList<TariffClaim> {
        val matched = filter(items) { candidate: TariffClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffClaims(store: TariffClaimStore, query: TariffClaimQuery): MutableList<TariffClaim> {
    return query.apply(store.list())
}
