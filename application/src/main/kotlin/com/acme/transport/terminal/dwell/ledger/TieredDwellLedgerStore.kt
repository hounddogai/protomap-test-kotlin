package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered dwell ledgers by code. */
interface TieredDwellLedgerStore {
    fun find(code: String): TieredDwellLedger?
    fun save(item: TieredDwellLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDwellLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDwellLedgerStore(
    var items: MutableMap<String, TieredDwellLedger>,
    var order: MutableList<String>,
) : TieredDwellLedgerStore {
    override fun find(code: String): TieredDwellLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDwellLedger) {
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

    override fun list(): MutableList<TieredDwellLedger> {
        val out = mutableListOf<TieredDwellLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDwellLedgerStore(): TieredDwellLedgerStore {
    return MemoryTieredDwellLedgerStore(mutableMapOf<String, TieredDwellLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDwellLedgerStore(
    var inner: TieredDwellLedgerStore,
    var cache: TtlCache<TieredDwellLedger>,
    var hits: Int,
    var misses: Int,
) : TieredDwellLedgerStore {
    override fun find(code: String): TieredDwellLedger? {
        val hit = (cache.get(code) as TieredDwellLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDwellLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDwellLedger> = inner.list()
}

fun newCachedTieredDwellLedgerStore(inner: TieredDwellLedgerStore, ttl: Int, clock: Clock): TieredDwellLedgerStore {
    return CachedTieredDwellLedgerStore(
        inner = inner,
        cache = newTtlCache<TieredDwellLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered dwell ledgers. Empty fields match all. */
data class TieredDwellLedgerQuery(
    var channel: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDwellLedger): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDwellLedger>): MutableList<TieredDwellLedger> {
        val matched = filter(items) { candidate: TieredDwellLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDwellLedgers(store: TieredDwellLedgerStore, query: TieredDwellLedgerQuery): MutableList<TieredDwellLedger> {
    return query.apply(store.list())
}
