package com.acme.transport.crewing.swap.rule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap ledgers by code. */
interface SwapLedgerStore {
    fun find(code: String): SwapLedger?
    fun save(item: SwapLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapLedgerStore(
    var items: MutableMap<String, SwapLedger>,
    var order: MutableList<String>,
) : SwapLedgerStore {
    override fun find(code: String): SwapLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapLedger) {
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

    override fun list(): MutableList<SwapLedger> {
        val out = mutableListOf<SwapLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapLedgerStore(): SwapLedgerStore {
    return MemorySwapLedgerStore(mutableMapOf<String, SwapLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapLedgerStore(
    var inner: SwapLedgerStore,
    var cache: TtlCache<SwapLedger>,
    var hits: Int,
    var misses: Int,
) : SwapLedgerStore {
    override fun find(code: String): SwapLedger? {
        val hit = (cache.get(code) as SwapLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapLedger> = inner.list()
}

fun newCachedSwapLedgerStore(inner: SwapLedgerStore, ttl: Int, clock: Clock): SwapLedgerStore {
    return CachedSwapLedgerStore(
        inner = inner,
        cache = newTtlCache<SwapLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap ledgers. Empty fields match all. */
data class SwapLedgerQuery(
    var locale: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapLedger): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapLedger>): MutableList<SwapLedger> {
        val matched = filter(items) { candidate: SwapLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapLedgers(store: SwapLedgerStore, query: SwapLedgerQuery): MutableList<SwapLedger> {
    return query.apply(store.list())
}
