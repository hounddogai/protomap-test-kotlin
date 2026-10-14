package com.acme.transport.terminal.traileryard.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard ledgers by code. */
interface TrailerYardLedgerStore {
    fun find(code: String): TrailerYardLedger?
    fun save(item: TrailerYardLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardLedgerStore(
    var items: MutableMap<String, TrailerYardLedger>,
    var order: MutableList<String>,
) : TrailerYardLedgerStore {
    override fun find(code: String): TrailerYardLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardLedger) {
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

    override fun list(): MutableList<TrailerYardLedger> {
        val out = mutableListOf<TrailerYardLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardLedgerStore(): TrailerYardLedgerStore {
    return MemoryTrailerYardLedgerStore(mutableMapOf<String, TrailerYardLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardLedgerStore(
    var inner: TrailerYardLedgerStore,
    var cache: TtlCache<TrailerYardLedger>,
    var hits: Int,
    var misses: Int,
) : TrailerYardLedgerStore {
    override fun find(code: String): TrailerYardLedger? {
        val hit = (cache.get(code) as TrailerYardLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardLedger> = inner.list()
}

fun newCachedTrailerYardLedgerStore(inner: TrailerYardLedgerStore, ttl: Int, clock: Clock): TrailerYardLedgerStore {
    return CachedTrailerYardLedgerStore(
        inner = inner,
        cache = newTtlCache<TrailerYardLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard ledgers. Empty fields match all. */
data class TrailerYardLedgerQuery(
    var segment: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardLedger): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<TrailerYardLedger>): MutableList<TrailerYardLedger> {
        val matched = filter(items) { candidate: TrailerYardLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardLedgers(store: TrailerYardLedgerStore, query: TrailerYardLedgerQuery): MutableList<TrailerYardLedger> {
    return query.apply(store.list())
}
