package com.acme.transport.crewing.swap.entry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict swap ledgers by code. */
interface StrictSwapLedgerStore {
    fun find(code: String): StrictSwapLedger?
    fun save(item: StrictSwapLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictSwapLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictSwapLedgerStore(
    var items: MutableMap<String, StrictSwapLedger>,
    var order: MutableList<String>,
) : StrictSwapLedgerStore {
    override fun find(code: String): StrictSwapLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictSwapLedger) {
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

    override fun list(): MutableList<StrictSwapLedger> {
        val out = mutableListOf<StrictSwapLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictSwapLedgerStore(): StrictSwapLedgerStore {
    return MemoryStrictSwapLedgerStore(mutableMapOf<String, StrictSwapLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictSwapLedgerStore(
    var inner: StrictSwapLedgerStore,
    var cache: TtlCache<StrictSwapLedger>,
    var hits: Int,
    var misses: Int,
) : StrictSwapLedgerStore {
    override fun find(code: String): StrictSwapLedger? {
        val hit = (cache.get(code) as StrictSwapLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictSwapLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictSwapLedger> = inner.list()
}

fun newCachedStrictSwapLedgerStore(inner: StrictSwapLedgerStore, ttl: Int, clock: Clock): StrictSwapLedgerStore {
    return CachedStrictSwapLedgerStore(
        inner = inner,
        cache = newTtlCache<StrictSwapLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict swap ledgers. Empty fields match all. */
data class StrictSwapLedgerQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictSwapLedger): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<StrictSwapLedger>): MutableList<StrictSwapLedger> {
        val matched = filter(items) { candidate: StrictSwapLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictSwapLedgers(store: StrictSwapLedgerStore, query: StrictSwapLedgerQuery): MutableList<StrictSwapLedger> {
    return query.apply(store.list())
}
