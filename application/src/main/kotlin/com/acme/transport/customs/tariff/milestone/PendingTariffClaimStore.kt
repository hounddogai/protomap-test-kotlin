package com.acme.transport.customs.tariff.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending tariff claims by code. */
interface PendingTariffClaimStore {
    fun find(code: String): PendingTariffClaim?
    fun save(item: PendingTariffClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTariffClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTariffClaimStore(
    var items: MutableMap<String, PendingTariffClaim>,
    var order: MutableList<String>,
) : PendingTariffClaimStore {
    override fun find(code: String): PendingTariffClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTariffClaim) {
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

    override fun list(): MutableList<PendingTariffClaim> {
        val out = mutableListOf<PendingTariffClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTariffClaimStore(): PendingTariffClaimStore {
    return MemoryPendingTariffClaimStore(mutableMapOf<String, PendingTariffClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTariffClaimStore(
    var inner: PendingTariffClaimStore,
    var cache: TtlCache<PendingTariffClaim>,
    var hits: Int,
    var misses: Int,
) : PendingTariffClaimStore {
    override fun find(code: String): PendingTariffClaim? {
        val hit = (cache.get(code) as PendingTariffClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTariffClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTariffClaim> = inner.list()
}

fun newCachedPendingTariffClaimStore(inner: PendingTariffClaimStore, ttl: Int, clock: Clock): PendingTariffClaimStore {
    return CachedPendingTariffClaimStore(
        inner = inner,
        cache = newTtlCache<PendingTariffClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending tariff claims. Empty fields match all. */
data class PendingTariffClaimQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTariffClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingTariffClaim>): MutableList<PendingTariffClaim> {
        val matched = filter(items) { candidate: PendingTariffClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTariffClaims(store: PendingTariffClaimStore, query: PendingTariffClaimQuery): MutableList<PendingTariffClaim> {
    return query.apply(store.list())
}
