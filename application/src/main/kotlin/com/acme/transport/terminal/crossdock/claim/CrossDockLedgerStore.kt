package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock ledgers by code. */
interface CrossDockLedgerStore {
    fun find(code: String): CrossDockLedger?
    fun save(item: CrossDockLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockLedgerStore(
    var items: MutableMap<String, CrossDockLedger>,
    var order: MutableList<String>,
) : CrossDockLedgerStore {
    override fun find(code: String): CrossDockLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockLedger) {
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

    override fun list(): MutableList<CrossDockLedger> {
        val out = mutableListOf<CrossDockLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockLedgerStore(): CrossDockLedgerStore {
    return MemoryCrossDockLedgerStore(mutableMapOf<String, CrossDockLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockLedgerStore(
    var inner: CrossDockLedgerStore,
    var cache: TtlCache<CrossDockLedger>,
    var hits: Int,
    var misses: Int,
) : CrossDockLedgerStore {
    override fun find(code: String): CrossDockLedger? {
        val hit = (cache.get(code) as CrossDockLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockLedger> = inner.list()
}

fun newCachedCrossDockLedgerStore(inner: CrossDockLedgerStore, ttl: Int, clock: Clock): CrossDockLedgerStore {
    return CachedCrossDockLedgerStore(
        inner = inner,
        cache = newTtlCache<CrossDockLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock ledgers. Empty fields match all. */
data class CrossDockLedgerQuery(
    var region: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockLedger): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<CrossDockLedger>): MutableList<CrossDockLedger> {
        val matched = filter(items) { candidate: CrossDockLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockLedgers(store: CrossDockLedgerStore, query: CrossDockLedgerQuery): MutableList<CrossDockLedger> {
    return query.apply(store.list())
}
