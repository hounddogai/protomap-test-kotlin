package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global backhaul ledgers by code. */
interface GlobalBackhaulLedgerStore {
    fun find(code: String): GlobalBackhaulLedger?
    fun save(item: GlobalBackhaulLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalBackhaulLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalBackhaulLedgerStore(
    var items: MutableMap<String, GlobalBackhaulLedger>,
    var order: MutableList<String>,
) : GlobalBackhaulLedgerStore {
    override fun find(code: String): GlobalBackhaulLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalBackhaulLedger) {
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

    override fun list(): MutableList<GlobalBackhaulLedger> {
        val out = mutableListOf<GlobalBackhaulLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalBackhaulLedgerStore(): GlobalBackhaulLedgerStore {
    return MemoryGlobalBackhaulLedgerStore(
        mutableMapOf<String, GlobalBackhaulLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalBackhaulLedgerStore(
    var inner: GlobalBackhaulLedgerStore,
    var cache: TtlCache<GlobalBackhaulLedger>,
    var hits: Int,
    var misses: Int,
) : GlobalBackhaulLedgerStore {
    override fun find(code: String): GlobalBackhaulLedger? {
        val hit = (cache.get(code) as GlobalBackhaulLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalBackhaulLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalBackhaulLedger> = inner.list()
}

fun newCachedGlobalBackhaulLedgerStore(inner: GlobalBackhaulLedgerStore, ttl: Int, clock: Clock): GlobalBackhaulLedgerStore {
    return CachedGlobalBackhaulLedgerStore(
        inner = inner,
        cache = newTtlCache<GlobalBackhaulLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global backhaul ledgers. Empty fields match all. */
data class GlobalBackhaulLedgerQuery(
    var country: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalBackhaulLedger): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalBackhaulLedger>): MutableList<GlobalBackhaulLedger> {
        val matched = filter(items) { candidate: GlobalBackhaulLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalBackhaulLedgers(store: GlobalBackhaulLedgerStore, query: GlobalBackhaulLedgerQuery): MutableList<GlobalBackhaulLedger> {
    return query.apply(store.list())
}
