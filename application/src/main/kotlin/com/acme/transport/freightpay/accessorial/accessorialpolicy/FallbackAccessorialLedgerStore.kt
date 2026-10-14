package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback accessorial ledgers by code. */
interface FallbackAccessorialLedgerStore {
    fun find(code: String): FallbackAccessorialLedger?
    fun save(item: FallbackAccessorialLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAccessorialLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAccessorialLedgerStore(
    var items: MutableMap<String, FallbackAccessorialLedger>,
    var order: MutableList<String>,
) : FallbackAccessorialLedgerStore {
    override fun find(code: String): FallbackAccessorialLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAccessorialLedger) {
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

    override fun list(): MutableList<FallbackAccessorialLedger> {
        val out = mutableListOf<FallbackAccessorialLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAccessorialLedgerStore(): FallbackAccessorialLedgerStore {
    return MemoryFallbackAccessorialLedgerStore(
        mutableMapOf<String, FallbackAccessorialLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAccessorialLedgerStore(
    var inner: FallbackAccessorialLedgerStore,
    var cache: TtlCache<FallbackAccessorialLedger>,
    var hits: Int,
    var misses: Int,
) : FallbackAccessorialLedgerStore {
    override fun find(code: String): FallbackAccessorialLedger? {
        val hit = (cache.get(code) as FallbackAccessorialLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAccessorialLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAccessorialLedger> = inner.list()
}

fun newCachedFallbackAccessorialLedgerStore(inner: FallbackAccessorialLedgerStore, ttl: Int, clock: Clock): FallbackAccessorialLedgerStore {
    return CachedFallbackAccessorialLedgerStore(
        inner = inner,
        cache = newTtlCache<FallbackAccessorialLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback accessorial ledgers. Empty fields match all. */
data class FallbackAccessorialLedgerQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAccessorialLedger): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAccessorialLedger>): MutableList<FallbackAccessorialLedger> {
        val matched = filter(items) { candidate: FallbackAccessorialLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAccessorialLedgers(store: FallbackAccessorialLedgerStore, query: FallbackAccessorialLedgerQuery): MutableList<FallbackAccessorialLedger> {
    return query.apply(store.list())
}
