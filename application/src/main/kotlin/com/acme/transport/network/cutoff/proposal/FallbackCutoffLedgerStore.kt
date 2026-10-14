package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback cutoff ledgers by code. */
interface FallbackCutoffLedgerStore {
    fun find(code: String): FallbackCutoffLedger?
    fun save(item: FallbackCutoffLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackCutoffLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackCutoffLedgerStore(
    var items: MutableMap<String, FallbackCutoffLedger>,
    var order: MutableList<String>,
) : FallbackCutoffLedgerStore {
    override fun find(code: String): FallbackCutoffLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackCutoffLedger) {
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

    override fun list(): MutableList<FallbackCutoffLedger> {
        val out = mutableListOf<FallbackCutoffLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackCutoffLedgerStore(): FallbackCutoffLedgerStore {
    return MemoryFallbackCutoffLedgerStore(
        mutableMapOf<String, FallbackCutoffLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackCutoffLedgerStore(
    var inner: FallbackCutoffLedgerStore,
    var cache: TtlCache<FallbackCutoffLedger>,
    var hits: Int,
    var misses: Int,
) : FallbackCutoffLedgerStore {
    override fun find(code: String): FallbackCutoffLedger? {
        val hit = (cache.get(code) as FallbackCutoffLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackCutoffLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackCutoffLedger> = inner.list()
}

fun newCachedFallbackCutoffLedgerStore(inner: FallbackCutoffLedgerStore, ttl: Int, clock: Clock): FallbackCutoffLedgerStore {
    return CachedFallbackCutoffLedgerStore(
        inner = inner,
        cache = newTtlCache<FallbackCutoffLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback cutoff ledgers. Empty fields match all. */
data class FallbackCutoffLedgerQuery(
    var segment: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackCutoffLedger): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackCutoffLedger>): MutableList<FallbackCutoffLedger> {
        val matched = filter(items) { candidate: FallbackCutoffLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackCutoffLedgers(store: FallbackCutoffLedgerStore, query: FallbackCutoffLedgerQuery): MutableList<FallbackCutoffLedger> {
    return query.apply(store.list())
}
