package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy cross dock ledgers by code. */
interface LegacyCrossDockLedgerStore {
    fun find(code: String): LegacyCrossDockLedger?
    fun save(item: LegacyCrossDockLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCrossDockLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCrossDockLedgerStore(
    var items: MutableMap<String, LegacyCrossDockLedger>,
    var order: MutableList<String>,
) : LegacyCrossDockLedgerStore {
    override fun find(code: String): LegacyCrossDockLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCrossDockLedger) {
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

    override fun list(): MutableList<LegacyCrossDockLedger> {
        val out = mutableListOf<LegacyCrossDockLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCrossDockLedgerStore(): LegacyCrossDockLedgerStore {
    return MemoryLegacyCrossDockLedgerStore(
        mutableMapOf<String, LegacyCrossDockLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCrossDockLedgerStore(
    var inner: LegacyCrossDockLedgerStore,
    var cache: TtlCache<LegacyCrossDockLedger>,
    var hits: Int,
    var misses: Int,
) : LegacyCrossDockLedgerStore {
    override fun find(code: String): LegacyCrossDockLedger? {
        val hit = (cache.get(code) as LegacyCrossDockLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCrossDockLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCrossDockLedger> = inner.list()
}

fun newCachedLegacyCrossDockLedgerStore(inner: LegacyCrossDockLedgerStore, ttl: Int, clock: Clock): LegacyCrossDockLedgerStore {
    return CachedLegacyCrossDockLedgerStore(
        inner = inner,
        cache = newTtlCache<LegacyCrossDockLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy cross dock ledgers. Empty fields match all. */
data class LegacyCrossDockLedgerQuery(
    var depot: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCrossDockLedger): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyCrossDockLedger>): MutableList<LegacyCrossDockLedger> {
        val matched = filter(items) { candidate: LegacyCrossDockLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCrossDockLedgers(store: LegacyCrossDockLedgerStore, query: LegacyCrossDockLedgerQuery): MutableList<LegacyCrossDockLedger> {
    return query.apply(store.list())
}
