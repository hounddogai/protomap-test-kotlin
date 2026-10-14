package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback rate card ledgers by code. */
interface FallbackRateCardLedgerStore {
    fun find(code: String): FallbackRateCardLedger?
    fun save(item: FallbackRateCardLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRateCardLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRateCardLedgerStore(
    var items: MutableMap<String, FallbackRateCardLedger>,
    var order: MutableList<String>,
) : FallbackRateCardLedgerStore {
    override fun find(code: String): FallbackRateCardLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRateCardLedger) {
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

    override fun list(): MutableList<FallbackRateCardLedger> {
        val out = mutableListOf<FallbackRateCardLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRateCardLedgerStore(): FallbackRateCardLedgerStore {
    return MemoryFallbackRateCardLedgerStore(
        mutableMapOf<String, FallbackRateCardLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRateCardLedgerStore(
    var inner: FallbackRateCardLedgerStore,
    var cache: TtlCache<FallbackRateCardLedger>,
    var hits: Int,
    var misses: Int,
) : FallbackRateCardLedgerStore {
    override fun find(code: String): FallbackRateCardLedger? {
        val hit = (cache.get(code) as FallbackRateCardLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRateCardLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRateCardLedger> = inner.list()
}

fun newCachedFallbackRateCardLedgerStore(inner: FallbackRateCardLedgerStore, ttl: Int, clock: Clock): FallbackRateCardLedgerStore {
    return CachedFallbackRateCardLedgerStore(
        inner = inner,
        cache = newTtlCache<FallbackRateCardLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback rate card ledgers. Empty fields match all. */
data class FallbackRateCardLedgerQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRateCardLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRateCardLedger>): MutableList<FallbackRateCardLedger> {
        val matched = filter(items) { candidate: FallbackRateCardLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRateCardLedgers(store: FallbackRateCardLedgerStore, query: FallbackRateCardLedgerQuery): MutableList<FallbackRateCardLedger> {
    return query.apply(store.list())
}
